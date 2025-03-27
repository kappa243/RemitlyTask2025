package io.github.kappa243.remitly2025;

import io.github.kappa243.remitly2025.model.BankItem;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.repositories.BanksRepository;
import io.github.kappa243.remitly2025.repositories.CountriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InitParser implements SmartInitializingSingleton {
    
    private final BanksRepository banksRepository;
    private final CountriesRepository countriesRepository;
    
    private final MongoTemplate mongoTemplate;
    
    @Override
    @Transactional
    public void afterSingletonsInstantiated() {
        
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
    
}
