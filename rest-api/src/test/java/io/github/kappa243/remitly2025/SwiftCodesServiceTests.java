package io.github.kappa243.remitly2025;

import io.github.kappa243.remitly2025.controllers.BankResponse;
import io.github.kappa243.remitly2025.controllers.CountryBanksResponse;
import io.github.kappa243.remitly2025.exceptions.BankAlreadyExistsException;
import io.github.kappa243.remitly2025.exceptions.BankNotFoundException;
import io.github.kappa243.remitly2025.exceptions.ChildBranchesFoundException;
import io.github.kappa243.remitly2025.exceptions.CountryNotExistsException;
import io.github.kappa243.remitly2025.exceptions.HeadBankNotFoundException;
import io.github.kappa243.remitly2025.model.BankItem;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.repositories.BanksRepository;
import io.github.kappa243.remitly2025.repositories.CountriesRepository;
import io.github.kappa243.remitly2025.services.SwiftCodesServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.projection.ProjectionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

public class SwiftCodesServiceTests extends BaseTestModule {
    
    @Mock
    BanksRepository banksRepository;
    
    @Mock
    CountriesRepository countriesRepository;
    
    @Autowired
    @Spy
    ProjectionFactory projectionFactory;
    
    @InjectMocks
    SwiftCodesServiceImpl swiftCodesService;
    
    
    CountryItem countryPL = new CountryItem("PL", "POLAND");
    
    BankItem bankItem = BankItem.builder()
        .swiftCode("ABCDEFGHXXX")
        .name("MAIN STREET BANK")
        .address("1234 Main St")
        .countryISO2(countryPL)
        .headquarter(true)
        .branches(new ArrayList<>())
        .build();
    
    BankResponse bankResponse;
    
    @BeforeEach
    public void beforeEach() {
        bankResponse = projectionFactory.createProjection(BankResponse.class, bankItem);
    }
    
    
    @Test
    public void whenGetBankBySwiftCodeAndBankExists_thenReturnBankResponse() {
        when(banksRepository.findBySwiftCode(bankItem.getSwiftCode())).thenReturn(Optional.of(bankResponse));
        
        assertThatCode(() -> {
            BankResponse response = swiftCodesService.getBankBySwiftCode(bankItem.getSwiftCode());
            
            assertThat(response.getSwiftCode()).isEqualTo(bankItem.getSwiftCode());
            assertThat(response.getCountryISO2()).isEqualTo(bankItem.getCountryISO2().getCountryISO2());
            assertThat(response.getCountryName()).isEqualTo(bankItem.getCountryISO2().getCountryName());
            assertThat(response.getName()).isEqualTo(bankItem.getName());
            assertThat(response.getAddress()).isEqualTo(bankItem.getAddress());
            assertThat(response.getHeadquarter()).isEqualTo(bankItem.isHeadquarter());
            assertThat(response.getBranches().size()).isEqualTo(bankItem.getBranches().size());
        }).doesNotThrowAnyException();
    }
    
    @Test
    public void whenGetBankBySwiftCodeAndBankDoesNotExist_thenThrowBankNotFoundException() {
        when(banksRepository.findBySwiftCode(bankItem.getSwiftCode())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> swiftCodesService.getBankBySwiftCode(bankItem.getSwiftCode()))
            .isInstanceOf(BankNotFoundException.class);
    }
    
    @Test
    public void whenSaveBankAndBankDoesNotExistsAndCountryExistsAndIsHeadquarter_thenSaveBank() {
        when(banksRepository.findById(bankItem.getSwiftCode())).thenReturn(Optional.empty());
        when(countriesRepository.findById(countryPL.getCountryISO2())).thenReturn(Optional.of(countryPL));
        when(banksRepository.save(bankItem)).thenReturn(bankItem);
        
        assertThatCode(() -> swiftCodesService.addBank(bankItem)).doesNotThrowAnyException();
    }
    
    @Test
    public void whenSaveBankAndBankDoesNotExistsAndCountryDoesNotExistsAndIsHeadquarter_thenSaveBank() {
        when(banksRepository.findById(bankItem.getSwiftCode())).thenReturn(Optional.empty());
        when(countriesRepository.findById(countryPL.getCountryISO2())).thenReturn(Optional.empty());
        when(countriesRepository.save(countryPL)).thenReturn(countryPL);
        when(banksRepository.save(bankItem)).thenReturn(bankItem);
        
        assertThatCode(() -> swiftCodesService.addBank(bankItem)).doesNotThrowAnyException();
    }
    
    @Test
    public void whenSaveBankAndBankExists_thenThrowBankAlreadyExistsException() {
        when(banksRepository.findById(bankItem.getSwiftCode())).thenReturn(Optional.of(bankItem));
        
        assertThatThrownBy(() -> swiftCodesService.addBank(bankItem))
            .isInstanceOf(BankAlreadyExistsException.class);
    }
    
    @Test
    public void whenSaveBankAndBankDoesNotExistsAndCountryExistsAndHasNotHeadquarter_thenThrowHeadBankNotFoundException() {
        BankItem branch = bankItem.toBuilder()
            .swiftCode("ABCDEFGHABC")
            .headquarter(false)
            .build();
        
        when(banksRepository.findById(branch.getSwiftCode())).thenReturn(Optional.empty());
        when(countriesRepository.findById(branch.getCountryISO2().getCountryISO2())).thenReturn(Optional.of(countryPL));
        when(banksRepository.save(branch)).thenReturn(branch);
        when(banksRepository.findById(bankItem.getSwiftCode())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> swiftCodesService.addBank(branch))
            .isInstanceOf(HeadBankNotFoundException.class);
    }
    
    @Test
    public void whenGetBanksByCountryISO2AndCountryExists_thenReturnBanks() {
        when(countriesRepository.findById(countryPL.getCountryISO2())).thenReturn(Optional.of(countryPL));
        when(banksRepository.findAllByCountryISO2_CountryISO2(countryPL.getCountryISO2())).thenReturn(List.of(bankResponse));
        
        assertThatCode(() -> {
            var banks = swiftCodesService.getBanksByCountryISO2(countryPL.getCountryISO2());
            assertThat(banks.getSwiftCodes().size()).isEqualTo(1);
            assertThat(banks.getSwiftCodes()).allSatisfy(bank -> assertThat(bank.getCountryISO2()).isEqualTo(countryPL.getCountryISO2()));
        }).doesNotThrowAnyException();
    }
    
    @Test
    public void whenGetBanksByCountryISO2AndCountryDoesNotExists_thenThrowCountryNotExistsException() {
        when(countriesRepository.findById(countryPL.getCountryISO2())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> swiftCodesService.getBanksByCountryISO2(countryPL.getCountryISO2()))
            .isInstanceOf(CountryNotExistsException.class);
    }
    
    @Test
    public void whenDeleteBankAndBankExists_thenDeleteBank() {
        when(banksRepository.findById(bankItem.getSwiftCode())).thenReturn(Optional.of(bankItem));
        
        assertThatCode(() -> swiftCodesService.deleteBank(bankItem.getSwiftCode())).doesNotThrowAnyException();
    }
    
    @Test
    public void whenDeleteBankAndBankDoesNotExists_thenThrowBankNotFoundException() {
        when(banksRepository.findById(bankItem.getSwiftCode())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> swiftCodesService.deleteBank(bankItem.getSwiftCode()))
            .isInstanceOf(BankNotFoundException.class);
    }
    
    @Test
    public void whenDeleteBankAndBankHasBranches_thenThrowChildBranchesFoundException() {
        BankItem branch = bankItem.toBuilder()
            .swiftCode("ABCDEFGHABC")
            .headquarter(false)
            .build();
        
        bankItem.getBranches().add(branch);
        
        when(banksRepository.findById(bankItem.getSwiftCode())).thenReturn(Optional.of(bankItem));
        
        assertThatThrownBy(() -> swiftCodesService.deleteBank(bankItem.getSwiftCode()))
            .isInstanceOf(ChildBranchesFoundException.class);
    }
}
