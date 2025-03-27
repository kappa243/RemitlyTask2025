package io.github.kappa243.remitly2025;

import io.github.kappa243.remitly2025.model.BankItem;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.parser.BankCSVParser;
import io.github.kappa243.remitly2025.repositories.BanksRepository;
import io.github.kappa243.remitly2025.repositories.CountriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {
    
    private final BanksRepository banksRepository;
    private final CountriesRepository countriesRepository;
    
    private final MongoTemplate mongoTemplate;
    
    private final BankCSVParser bankCSVParser;
    
    private void internalTest() {
        if (mongoTemplate.collectionExists(BankItem.class) || mongoTemplate.collectionExists(CountryItem.class)) {
            // clear all data
            mongoTemplate.dropCollection(BankItem.class);
            mongoTemplate.dropCollection(CountryItem.class);
            
            mongoTemplate.indexOps(BankItem.class).ensureIndex(new Index().on("countryCode", Sort.Direction.ASC).named("countryCode_"));
        }
        
        var country_pl = new CountryItem("PL", "POLAND");
        
        countriesRepository.save(country_pl);
        
        var bank_child_a = new BankItem("BREXPLPWWRO", "MBANK S.A. (FORMERLY BRE BANK S.A.)", "PL. JANA PAWLA II 9  WROCLAW, DOLNOSLASKIE, 50-136", false, country_pl);
        var bank_child_b = new BankItem("BREXPLPWWAL", "MBANK S.A. (FORMERLY BRE BANK S.A.)", "SIENKIEWICZA 2  WALBRZYCH, DOLNOSLASKIE, 58-300", false, country_pl);
        var bank_head = new BankItem("BREXPLPWXXX", "MBANK S.A. (FORMERLY BRE BANK S.A.)", "UL. PROSTA 18  WARSZAWA, MAZOWIECKIE, 00-850", true, country_pl, List.of(bank_child_a, bank_child_b));
        
        banksRepository.save(bank_child_a);
        banksRepository.save(bank_child_b);
        banksRepository.save(bank_head);
    }
    
    @Override
    public void run(String... args) {
//        internalTest();
        
        Pair<Set<CountryItem>, Set<BankItem>> entries = null;
        try {
            entries = bankCSVParser.parseCSV();
            
        } catch (IOException e) {
            log.error("Error while parsing CSV banks data", e);
        } catch (RuntimeException e) {
            // if openCSV throws runtime this is only moment we can catch it before application crash
            log.error("Something went wrong during CSV parsing", e);
        }
        
        if (entries != null) {
            countriesRepository.saveAll(entries.getFirst());
            banksRepository.saveAll(entries.getSecond());
        }
    }
}
