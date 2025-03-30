package io.github.kappa243.remitly2025.parser;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.model.SwiftCodeItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CSVParser {
    
    private Pair<CountryItem, SwiftCodeItem> mapEntry(BankCSVEntry entry) {
        CountryItem countryItem = CountryItem.builder()
            .countryISO2(entry.getCountryISO2())
            .countryName(entry.getCountryName())
            .build();
        
        SwiftCodeItem swiftCodeItem = SwiftCodeItem.builder()
            .swiftCode(entry.getSwiftCode())
            .bankName(entry.getBankName().trim().toUpperCase())
            .address(entry.getAddress().trim().toUpperCase())
            .countryISO2(countryItem)
            .build();
        
        return Pair.of(countryItem, swiftCodeItem);
    }
    
    public Pair<Set<CountryItem>, Set<SwiftCodeItem>> parseCSV() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource csvResource = resolver.getResource("swift_codes.csv");
        
        Set<CountryItem> countries = new HashSet<>();
        Set<SwiftCodeItem> swiftCodes = new HashSet<>();
        
        Map<String, SwiftCodeItem> headSwiftCodes = new HashMap<>();
        
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper
            .schemaFor(BankCSVEntry.class)
            .withHeader()
            .withColumnReordering(true);
        
        try (MappingIterator<BankCSVEntry> iter = new CsvMapper()
            .readerFor(BankCSVEntry.class)
            .with(schema)
            .readValues(csvResource.getInputStream())) {
            
            Map<Boolean, List<BankCSVEntry>> partitioned = iter.readAll().stream()
                .collect(Collectors.partitioningBy(entry -> entry.getSwiftCode().endsWith("XXX")));
            
            List<BankCSVEntry> headEntries = partitioned.get(true);
            List<BankCSVEntry> branchEntries = partitioned.get(false);
            
            headEntries.forEach(entry -> {
                var mapped = mapEntry(entry);
                
                SwiftCodeItem swiftCodeItem = mapped.getSecond().toBuilder()
                    .headquarter(true)
                    .branches(new ArrayList<>())
                    .build();
                
                String swiftInit = entry.getSwiftCode().substring(0, 8);
                
                countries.add(mapped.getFirst());
                swiftCodes.add(swiftCodeItem);
                headSwiftCodes.put(swiftInit, swiftCodeItem);
            });
            
            branchEntries.forEach(entry -> {
                var mapped = mapEntry(entry);
                
                SwiftCodeItem swiftCodeItem = mapped.getSecond().toBuilder()
                    .headquarter(false)
                    .build();
                
                String swiftInit = entry.getSwiftCode().substring(0, 8);
                SwiftCodeItem headBank = headSwiftCodes.get(swiftInit);
                
                if (headBank == null) {
                    log.warn("Detected branch without head SWIFT code: {}", entry.getSwiftCode() + "  Skipping.");
                    return;
                } else {
                    headBank.getBranches().add(swiftCodeItem);
                }
                
                countries.add(mapped.getFirst());
                swiftCodes.add(swiftCodeItem);
            });
        }
        
        return Pair.of(countries, swiftCodes);
    }
}
