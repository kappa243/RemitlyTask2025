package io.github.kappa243.remitly2025;


import io.github.kappa243.remitly2025.controllers.ReducedSwiftCodeResponse;
import io.github.kappa243.remitly2025.controllers.SwiftCodeResponse;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.model.SwiftCodeItem;
import io.github.kappa243.remitly2025.repositories.CountriesRepository;
import io.github.kappa243.remitly2025.repositories.SwiftCodesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SwiftCodesRepositoryTests extends BaseTestModule {
    
    @Autowired
    private SwiftCodesRepository swiftCodesRepository;
    
    @Autowired
    private CountriesRepository countriesRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @BeforeEach
    public void fillDatabase() {
        if (mongoTemplate.collectionExists(SwiftCodeItem.class) || mongoTemplate.collectionExists(CountryItem.class)) {
            // clear all data
            mongoTemplate.dropCollection(SwiftCodeItem.class);
            mongoTemplate.dropCollection(CountryItem.class);
            
            mongoTemplate.indexOps(SwiftCodeItem.class).ensureIndex(new Index().on("countryISO2", Sort.Direction.ASC).named("countryISO2_"));
        }
        
        var country = new CountryItem("PL", "POLAND");
        
        countriesRepository.save(country);
        
        var swiftCodeChildA = new SwiftCodeItem("BREXPLPWWRO", "MBANK S.A. (FORMERLY BRE BANK S.A.)", "PL. JANA PAWLA II 9  WROCLAW, DOLNOSLASKIE, 50-136", false, country);
        var swiftCodeChildN = new SwiftCodeItem("BREXPLPWWAL", "MBANK S.A. (FORMERLY BRE BANK S.A.)", "SIENKIEWICZA 2  WALBRZYCH, DOLNOSLASKIE, 58-300", false, country);
        var headSwiftCode = new SwiftCodeItem("BREXPLPWXXX", "MBANK S.A. (FORMERLY BRE BANK S.A.)", "UL. PROSTA 18  WARSZAWA, MAZOWIECKIE, 00-850", true, country, List.of(swiftCodeChildA, swiftCodeChildN));
        
        swiftCodesRepository.save(swiftCodeChildA);
        swiftCodesRepository.save(swiftCodeChildN);
        swiftCodesRepository.save(headSwiftCode);
    }
    
    
    CountryItem countryPL = new CountryItem("PL", "POLAND");
    
    SwiftCodeItem swiftCodeData = SwiftCodeItem.builder()
        .swiftCode("ABCDEFGHXXX")
        .bankName("Main Street Bank")
        .address("1234 Main St")
        .countryISO2(countryPL)
        .headquarter(true)
        .branches(new ArrayList<>())
        .build();
    
    @Test
    public void whenSaveSwiftCodeData_thenSwiftCodeDataExists() {
        int size = swiftCodesRepository.findAll().size();
        
        System.out.println(swiftCodesRepository.findAll().stream().map(SwiftCodeItem::getSwiftCode).toList());
        
        swiftCodesRepository.save(swiftCodeData);
        
        
        System.out.println(swiftCodesRepository.findAll().stream().map(SwiftCodeItem::getSwiftCode).toList());
        assertThat(swiftCodesRepository.findAll().size()).isEqualTo(size + 1);
        
        Optional<SwiftCodeItem> savedSwiftCodeData = swiftCodesRepository.findById(swiftCodeData.getSwiftCode());
        assertThat(savedSwiftCodeData).isPresent();
    }
    
    @Test
    public void whenFindSwiftCodeDataByCodeAndSwiftCodeDataExists_thenReturnSwiftCodeResponse() {
        swiftCodesRepository.save(swiftCodeData);
        Optional<SwiftCodeResponse> existingSwiftCodeResponse = swiftCodesRepository.findBySwiftCode(swiftCodeData.getSwiftCode());
        
        assertThat(existingSwiftCodeResponse).isPresent();
        
        SwiftCodeResponse swiftCodeResponse = existingSwiftCodeResponse.get();
        assertThat(swiftCodeResponse.getSwiftCode()).isEqualTo(swiftCodeData.getSwiftCode());
        assertThat(swiftCodeResponse.getCountryISO2()).isEqualTo(swiftCodeData.getCountryISO2().getCountryISO2());
        assertThat(swiftCodeResponse.getCountryName()).isEqualTo(swiftCodeData.getCountryISO2().getCountryName());
        assertThat(swiftCodeResponse.getBankName()).isEqualTo(swiftCodeData.getBankName());
        assertThat(swiftCodeResponse.getAddress()).isEqualTo(swiftCodeData.getAddress());
        assertThat(swiftCodeResponse.getHeadquarter()).isEqualTo(swiftCodeData.isHeadquarter());
        assertThat(swiftCodeResponse.getBranches()).isEqualTo(swiftCodeData.getBranches());
    }
    
    @Test
    public void whenFindSwiftCodeDataByCodeAndSwiftCodeDataDoesNotExist_thenReturnEmpty() {
        Optional<SwiftCodeResponse> nonExistingSwiftCodeResponse = swiftCodesRepository.findBySwiftCode("ABCDEFGHIJK");
        
        assertThat(nonExistingSwiftCodeResponse).isEmpty();
    }
    
    @Test
    public void whenFindSwiftCodeDataByCodeAndSwiftCodeDataHasBranches_thenReturnSwiftCodeResponseWithBranches() {
        SwiftCodeItem branchSwiftCodeData = SwiftCodeItem.builder()
            .swiftCode("ABCDEFGHABC")
            .bankName("Main Street Bank")
            .address("3333 Side St")
            .countryISO2(countryPL)
            .headquarter(false)
            .build();
        
        swiftCodeData.getBranches().add(branchSwiftCodeData);
        
        swiftCodesRepository.save(branchSwiftCodeData);
        swiftCodesRepository.save(swiftCodeData);
        
        Optional<SwiftCodeResponse> savedHeadSwiftCodeResponse = swiftCodesRepository.findBySwiftCode(swiftCodeData.getSwiftCode());
        Optional<SwiftCodeResponse> savedBranchSwiftCodeResponse = swiftCodesRepository.findBySwiftCode(branchSwiftCodeData.getSwiftCode());
        
        assertThat(savedHeadSwiftCodeResponse).isPresent();
        assertThat(savedBranchSwiftCodeResponse).isPresent();
        
        SwiftCodeResponse responseHeadSwiftCode = savedHeadSwiftCodeResponse.get();
        
        assertThat(responseHeadSwiftCode.getBranches().size()).isEqualTo(1);
        assertThat(responseHeadSwiftCode.getBranches().get(0).getSwiftCode()).isEqualTo(branchSwiftCodeData.getSwiftCode());
    }
    
    @Test
    public void whenFindSwiftCodesByCountryISO2_thenReturnReducedSwiftCodeResponses() {
        List<ReducedSwiftCodeResponse> savedSwiftCodeResponses = swiftCodesRepository.findAllByCountryISO2_CountryISO2(countryPL.getCountryISO2());
        
        assertThat(savedSwiftCodeResponses).isNotEmpty();
        assertThat(savedSwiftCodeResponses.size()).isEqualTo(3);
        assertThat(savedSwiftCodeResponses).allSatisfy(bank -> assertThat(bank.getCountryISO2()).isEqualTo(countryPL.getCountryISO2()));
    }
    
    @Test
    public void whenDeleteSwiftCodeData_thenReturnEmpty() {
        swiftCodesRepository.save(swiftCodeData);
        
        swiftCodesRepository.delete(swiftCodeData);
        
        Optional<SwiftCodeResponse> bank = swiftCodesRepository.findBySwiftCode(swiftCodeData.getSwiftCode());
        
        assertThat(bank).isEmpty();
    }
}
