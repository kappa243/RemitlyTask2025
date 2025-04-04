package io.github.kappa243.remitly2025;

import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.model.SwiftCodeItem;
import io.github.kappa243.remitly2025.parser.CSVParser;
import io.github.kappa243.remitly2025.repositories.CountriesRepository;
import io.github.kappa243.remitly2025.repositories.SwiftCodesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {
    
    private final SwiftCodesRepository swiftCodesRepository;
    private final CountriesRepository countriesRepository;
    
    private final MongoTemplate mongoTemplate;
    
    private final CSVParser bankCSVParser;
    
    @Override
    public void run(String... args) {
        if (!mongoTemplate.collectionExists(SwiftCodeItem.class) || !mongoTemplate.collectionExists(CountryItem.class)) {
            log.info("Initializing database with csv data");
            
            mongoTemplate.indexOps(SwiftCodeItem.class).ensureIndex(new Index().on("countryISO2", Sort.Direction.ASC).named("countryISO2_"));
            
            Pair<Set<CountryItem>, Set<SwiftCodeItem>> entries = null;
            try {
                entries = bankCSVParser.parseCSV();
                
            } catch (IOException e) {
                log.error("Error while parsing CSV banks data", e);
            } catch (RuntimeException e) {
                log.error("Something went wrong during CSV parsing", e);
            }
            
            if (entries != null) {
                countriesRepository.saveAll(entries.getFirst());
                swiftCodesRepository.saveAll(entries.getSecond());
            }
        }
    }
}