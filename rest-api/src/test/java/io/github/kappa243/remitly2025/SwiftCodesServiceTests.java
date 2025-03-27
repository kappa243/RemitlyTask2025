package io.github.kappa243.remitly2025;

import io.github.kappa243.remitly2025.controllers.BankResponse;
import io.github.kappa243.remitly2025.exceptions.BankNotFoundException;
import io.github.kappa243.remitly2025.model.BankItem;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.repositories.BanksRepository;
import io.github.kappa243.remitly2025.services.SwiftCodesServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

public class SwiftCodesServiceTests extends CommonTestModule {
    
    @Mock
    BanksRepository banksRepository;
    
    @InjectMocks
    SwiftCodesServiceImpl swiftCodesService;
    
    ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    
    
    CountryItem countryPL = new CountryItem("PL", "POLAND");
    
    BankItem bankItem = BankItem.builder()
        .swiftCode("ABCDEFGHXXX")
        .name("Main Street Bank")
        .address("1234 Main St")
        .countryCode(countryPL)
        .isHeadquarter(true)
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
            assertThat(response.getCountryCode()).isEqualTo(bankItem.getCountryCode().getCountryCode());
            assertThat(response.getCountryName()).isEqualTo(bankItem.getCountryCode().getCountryName());
            assertThat(response.getName()).isEqualTo(bankItem.getName());
            assertThat(response.getAddress()).isEqualTo(bankItem.getAddress());
            assertThat(response.getisHeadquarter()).isEqualTo(bankItem.isHeadquarter());
            assertThat(response.getBranches().size()).isEqualTo(bankItem.getBranches().size());
        }).doesNotThrowAnyException();
    }
    
    @Test
    public void whenGetBankBySwiftCodeAndBankDoesNotExist_thenThrowBankNotFoundException() {
        when(banksRepository.findBySwiftCode(bankItem.getSwiftCode())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> swiftCodesService.getBankBySwiftCode(bankItem.getSwiftCode()))
            .isInstanceOf(BankNotFoundException.class);
    }
}
