package io.github.kappa243.remitly2025;


import io.github.kappa243.remitly2025.controllers.BankResponse;
import io.github.kappa243.remitly2025.model.BankItem;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.repositories.BanksRepository;
import io.github.kappa243.remitly2025.repositories.CountriesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class BanksRepositoryTests extends CommonTestModule {
    
    @Autowired
    private BanksRepository banksRepository;
    
    @Autowired
    private CountriesRepository countriesRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @BeforeEach
    public void fillDatabase() {
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
    
    
    CountryItem countryPL = new CountryItem("PL", "POLAND");
    
    BankItem bankItem = BankItem.builder()
        .swiftCode("ABCDEFGHXXX")
        .name("Main Street Bank")
        .address("1234 Main St")
        .countryCode(countryPL)
        .isHeadquarter(true)
        .branches(new ArrayList<>())
        .build();
    
    @Test
    public void whenSaveBank_thenBankExists() {
        int size = banksRepository.findAll().size();
        
        System.out.println(banksRepository.findAll().stream().map(BankItem::getSwiftCode).toList());
        
        banksRepository.save(bankItem);
        
        
        System.out.println(banksRepository.findAll().stream().map(BankItem::getSwiftCode).toList());
        assertThat(banksRepository.findAll().size()).isEqualTo(size + 1);
        
        Optional<BankItem> bank = banksRepository.findById(bankItem.getSwiftCode());
        assertThat(bank).isPresent();
    }
    
    @Test
    public void whenFindBankByCodeAndBankExists_thenReturnBankResponse() {
        banksRepository.save(bankItem);
        Optional<BankResponse> bank = banksRepository.findBySwiftCode(bankItem.getSwiftCode());
        
        assertThat(bank).isPresent();
        
        BankResponse resBankItem = bank.get();
        assertThat(resBankItem.getSwiftCode()).isEqualTo(bankItem.getSwiftCode());
        assertThat(resBankItem.getCountryCode()).isEqualTo(bankItem.getCountryCode().getCountryCode());
        assertThat(resBankItem.getCountryName()).isEqualTo(bankItem.getCountryCode().getCountryName());
        assertThat(resBankItem.getName()).isEqualTo(bankItem.getName());
        assertThat(resBankItem.getAddress()).isEqualTo(bankItem.getAddress());
        assertThat(resBankItem.getisHeadquarter()).isEqualTo(bankItem.isHeadquarter());
        assertThat(resBankItem.getBranches()).isEqualTo(bankItem.getBranches());
    }
    
    @Test
    public void whenFindBankByCodeAndBankDoesNotExist_thenReturnEmpty() {
        Optional<BankResponse> bank = banksRepository.findBySwiftCode("ABCDEFGHIJK");
        
        assertThat(bank).isEmpty();
    }
    
    @Test
    public void whenFindBankByCodeAndBankHasBranches_thenReturnBankResponseWithBranches() {
        BankItem branch = BankItem.builder()
            .swiftCode("ABCDEFGHABC")
            .name("Main Street Bank")
            .address("3333 Side St")
            .countryCode(countryPL)
            .isHeadquarter(false)
            .build();
        
        bankItem.getBranches().add(branch);
        
        banksRepository.save(branch);
        banksRepository.save(bankItem);
        
        Optional<BankResponse> headBank = banksRepository.findBySwiftCode(bankItem.getSwiftCode());
        Optional<BankResponse> branchBank = banksRepository.findBySwiftCode(branch.getSwiftCode());
        
        assertThat(headBank).isPresent();
        assertThat(branchBank).isPresent();
        
        BankResponse resHeadBank = headBank.get();
        
        assertThat(resHeadBank.getBranches().size()).isEqualTo(1);
        assertThat(resHeadBank.getBranches().get(0).getSwiftCode()).isEqualTo(branch.getSwiftCode());
    }
    
    @Test
    public void whenDeleteBank_thenReturnEmpty() {
        banksRepository.save(bankItem);
        
        banksRepository.delete(bankItem);
        
        Optional<BankResponse> bank = banksRepository.findBySwiftCode(bankItem.getSwiftCode());
        
        assertThat(bank).isEmpty();
    }
}
