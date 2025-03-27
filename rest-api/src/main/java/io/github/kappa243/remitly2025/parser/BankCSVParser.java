package io.github.kappa243.remitly2025.parser;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import io.github.kappa243.remitly2025.model.BankItem;
import io.github.kappa243.remitly2025.model.CountryItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class BankCSVParser {
    
    static final Map<String, String> COLUMN_MAPPING = Map.of(
        "SWIFT CODE", "swiftCode",
        "NAME", "name",
        "ADDRESS", "address",
        "COUNTRY NAME", "countryName",
        "COUNTRY ISO2 CODE", "countryCode"
    );
    
    private void parseEntries(InputStream is, Set<CountryItem> countries, Set<BankItem> banks) throws IOException {
        
        Map<String, BankItem> headBanks = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            HeaderColumnNameTranslateMappingStrategy<BankCSVEntry> strategy = new HeaderColumnNameTranslateMappingStrategy<>();
            strategy.setType(BankCSVEntry.class);
            strategy.setColumnMapping(BankCSVParser.COLUMN_MAPPING);
            
            CsvToBean<BankCSVEntry> csvToBean = new CsvToBeanBuilder<BankCSVEntry>(br)
                .withMappingStrategy(strategy)
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreQuotations(false)
                .build();
            
            csvToBean.parse().stream()
                .sorted(Comparator.comparing((BankCSVEntry entry) -> entry.getSwiftCode().substring(entry.getSwiftCode().length() - 3)).reversed())
                .forEach(entry -> {
                    CountryItem countryItem = CountryItem.builder()
                        .countryCode(entry.getCountryCode())
                        .countryName(entry.getCountryName())
                        .build();
                    
                    BankItem.BankItemBuilder bankBuilder = BankItem.builder()
                        .swiftCode(entry.getSwiftCode())
                        .name(entry.getName().trim().toUpperCase())
                        .address(entry.getAddress().trim().toUpperCase())
                        .countryCode(countryItem);
                    
                    if (entry.getSwiftCode().endsWith("XXX")) {
                        String swiftInit = entry.getSwiftCode().substring(0, 8);
                        
                        bankBuilder.isHeadquarter(true);
                        bankBuilder.branches(new ArrayList<>()); // TODO make empty; configure adding outside of parser with service providing checking for list
                        
                        headBanks.put(swiftInit, bankBuilder.build());
                    } else {
                        bankBuilder.isHeadquarter(false);
                        
                        String swiftInit = entry.getSwiftCode().substring(0, 8);
                        BankItem headBank = headBanks.get(swiftInit);
                        
                        if (headBank == null) {
                            log.warn("Detected branch without head bank: {}", entry.getSwiftCode() + "  Adding without parent.");
                        } else {
                            headBank.getBranches().add(bankBuilder.build());
                        }
                    }
                    
                    countries.add(countryItem);
                    banks.add(bankBuilder.build());
                });
        }
    }
    
    public Pair<Set<CountryItem>, Set<BankItem>> parseCSV() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource csvResource = resolver.getResource("swift_codes.csv");
        
        InputStream is = csvResource.getInputStream();
        
        Set<CountryItem> countries = new HashSet<>();
        Set<BankItem> banks = new HashSet<>();
        
        parseEntries(is, countries, banks);
        
        return Pair.of(countries, banks);
    }
}
