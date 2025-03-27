package io.github.kappa243.remitly2025;

import io.github.kappa243.remitly2025.model.BankItem;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.repositories.CountriesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CountriesRepositoryTests extends CommonTestModule {
    
    @Autowired
    private CountriesRepository countriesRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    CountryItem countryPL = new CountryItem("PL", "POLAND");
    
    @BeforeEach
    public void fillDatabase() {
        if (mongoTemplate.collectionExists(BankItem.class) || mongoTemplate.collectionExists(CountryItem.class)) {
            // clear all data
            mongoTemplate.dropCollection(BankItem.class);
            mongoTemplate.dropCollection(CountryItem.class);
            
            mongoTemplate.indexOps(BankItem.class).ensureIndex(new Index().on("countryCode", Sort.Direction.ASC).named("countryCode_"));
        }
        
        countriesRepository.save(countryPL);
    }
    
    @Test
    public void whenSaveCountry_thenCountryExists() {
        countriesRepository.save(countryPL);
        
        Optional<CountryItem> foundCountry = countriesRepository.findById(countryPL.getCountryCode());
        assertThat(foundCountry).isPresent();
        assertThat(foundCountry.get().getCountryCode()).isEqualTo(countryPL.getCountryCode());
        assertThat(foundCountry.get().getCountryName()).isEqualTo(countryPL.getCountryName());
    }
    
    @Test
    public void whenFindCountryByCodeAndCountryDoesNotExist_thenReturnEmpty() {
        Optional<CountryItem> country = countriesRepository.findById("XX");
        assertThat(country).isEmpty();
    }
    
    @Test
    public void whenDeleteCountry_thenCountryDoesNotExist() {
        countriesRepository.save(countryPL);
        
        countriesRepository.delete(countryPL);
        
        Optional<CountryItem> foundCountry = countriesRepository.findById(countryPL.getCountryCode());
        assertThat(foundCountry).isEmpty();
    }
    
}
