package io.github.kappa243.remitly2025;

import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.model.SwiftCodeItem;
import io.github.kappa243.remitly2025.repositories.CountriesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class CountriesRepositoryTests extends BaseTestModule {
    
    @Autowired
    private CountriesRepository countriesRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    CountryItem countryPL = new CountryItem("PL", "POLAND");
    
    @BeforeEach
    public void fillDatabase() {
        if (mongoTemplate.collectionExists(SwiftCodeItem.class) || mongoTemplate.collectionExists(CountryItem.class)) {
            // clear all data
            mongoTemplate.dropCollection(SwiftCodeItem.class);
            mongoTemplate.dropCollection(CountryItem.class);
            
            mongoTemplate.indexOps(SwiftCodeItem.class).ensureIndex(new Index().on("countryISO2", Sort.Direction.ASC).named("countryISO2_"));
        }
        
        countriesRepository.save(countryPL);
    }
    
    @Test
    public void whenSaveCountryData_thenCountryDataExists() {
        countriesRepository.save(countryPL);
        
        Optional<CountryItem> savedCountryData = countriesRepository.findById(countryPL.getCountryISO2());
        assertThat(savedCountryData).isPresent();
        assertThat(savedCountryData.get().getCountryISO2()).isEqualTo(countryPL.getCountryISO2());
        assertThat(savedCountryData.get().getCountryName()).isEqualTo(countryPL.getCountryName());
    }
    
    @Test
    public void whenFindCountryDataByCodeAndCountryDataDoesNotExist_thenReturnEmpty() {
        Optional<CountryItem> nonExistingCountryData = countriesRepository.findById("XX");
        assertThat(nonExistingCountryData).isEmpty();
    }
    
    @Test
    public void whenDeleteCountryData_thenCountryDataDoesNotExist() {
        countriesRepository.save(countryPL);
        
        countriesRepository.delete(countryPL);
        
        Optional<CountryItem> nonExistingCountryData = countriesRepository.findById(countryPL.getCountryISO2());
        assertThat(nonExistingCountryData).isEmpty();
    }
    
}
