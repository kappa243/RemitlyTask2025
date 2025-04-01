package io.github.kappa243.remitly2025;

import io.github.kappa243.remitly2025.controllers.SwiftCodeResponse;
import io.github.kappa243.remitly2025.exceptions.ChildSwiftCodesFoundException;
import io.github.kappa243.remitly2025.exceptions.CountryNotExistsException;
import io.github.kappa243.remitly2025.exceptions.HeadSwiftCodeNotFoundException;
import io.github.kappa243.remitly2025.exceptions.SwiftCodeAlreadyExistsException;
import io.github.kappa243.remitly2025.exceptions.SwiftCodeNotFoundException;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.model.SwiftCodeItem;
import io.github.kappa243.remitly2025.repositories.CountriesRepository;
import io.github.kappa243.remitly2025.repositories.SwiftCodesRepository;
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
    SwiftCodesRepository swiftCodesRepository;
    
    @Mock
    CountriesRepository countriesRepository;
    
    @Autowired
    @Spy
    ProjectionFactory projectionFactory;
    
    @InjectMocks
    SwiftCodesServiceImpl swiftCodesService;
    
    
    CountryItem countryPL = new CountryItem("PL", "POLAND");
    
    SwiftCodeItem swiftCodeData = SwiftCodeItem.builder()
        .swiftCode("ABCDEFGHXXX")
        .bankName("MAIN STREET BANK")
        .address("1234 Main St")
        .countryISO2(countryPL)
        .headquarter(true)
        .branches(new ArrayList<>())
        .build();
    
    SwiftCodeResponse swiftCodeResponse;
    
    @BeforeEach
    public void beforeEach() {
        swiftCodeResponse = projectionFactory.createProjection(SwiftCodeResponse.class, swiftCodeData);
    }
    
    
    @Test
    public void whenGetSwiftCodeDataByCodeAndSwiftCodeDataExists_thenReturnSwiftCodeResponse() {
        when(swiftCodesRepository.findBySwiftCode(swiftCodeData.getSwiftCode())).thenReturn(Optional.of(swiftCodeResponse));
        
        assertThatCode(() -> {
            SwiftCodeResponse response = swiftCodesService.getSwiftCodeDataBySwiftCode(swiftCodeData.getSwiftCode());
            
            assertThat(response.getSwiftCode()).isEqualTo(swiftCodeData.getSwiftCode());
            assertThat(response.getCountryISO2()).isEqualTo(swiftCodeData.getCountryISO2().getCountryISO2());
            assertThat(response.getCountryName()).isEqualTo(swiftCodeData.getCountryISO2().getCountryName());
            assertThat(response.getBankName()).isEqualTo(swiftCodeData.getBankName());
            assertThat(response.getAddress()).isEqualTo(swiftCodeData.getAddress());
            assertThat(response.getHeadquarter()).isEqualTo(swiftCodeData.isHeadquarter());
            assertThat(response.getBranches().size()).isEqualTo(swiftCodeData.getBranches().size());
        }).doesNotThrowAnyException();
    }
    
    @Test
    public void whenGetSwiftCodeDataByCodeAndSwiftCodeDataDoesNotExist_thenThrowSwiftCodeNotFoundException() {
        when(swiftCodesRepository.findBySwiftCode(swiftCodeData.getSwiftCode())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> swiftCodesService.getSwiftCodeDataBySwiftCode(swiftCodeData.getSwiftCode()))
            .isInstanceOf(SwiftCodeNotFoundException.class);
    }
    
    @Test
    public void whenSaveSwiftCodeDataAndSwiftCodeDataDoesNotExistsAndCountryExistsAndIsHeadquarter_thenSaveSwiftCodeData() {
        when(swiftCodesRepository.findById(swiftCodeData.getSwiftCode())).thenReturn(Optional.empty());
        when(countriesRepository.findById(countryPL.getCountryISO2())).thenReturn(Optional.of(countryPL));
        when(swiftCodesRepository.save(swiftCodeData)).thenReturn(swiftCodeData);
        
        assertThatCode(() -> swiftCodesService.addSwiftCodeData(swiftCodeData)).doesNotThrowAnyException();
    }
    
    @Test
    public void whenSaveSwiftCodeDataAndSwiftCodeDataDoesNotExistsAndCountryDoesNotExistsAndIsHeadquarter_thenSaveSwiftCodeData() {
        when(swiftCodesRepository.findById(swiftCodeData.getSwiftCode())).thenReturn(Optional.empty());
        when(countriesRepository.findById(countryPL.getCountryISO2())).thenReturn(Optional.empty());
        when(countriesRepository.save(countryPL)).thenReturn(countryPL);
        when(swiftCodesRepository.save(swiftCodeData)).thenReturn(swiftCodeData);
        
        assertThatCode(() -> swiftCodesService.addSwiftCodeData(swiftCodeData)).doesNotThrowAnyException();
    }
    
    @Test
    public void whenSaveSwiftCodeDataAndSwiftCodeDataExists_thenThrowSwiftCodeAlreadyExistsException() {
        when(swiftCodesRepository.findById(swiftCodeData.getSwiftCode())).thenReturn(Optional.of(swiftCodeData));
        
        assertThatThrownBy(() -> swiftCodesService.addSwiftCodeData(swiftCodeData))
            .isInstanceOf(SwiftCodeAlreadyExistsException.class);
    }
    
    @Test
    public void whenSaveSwiftCodeDataAndSwiftCodeDataDoesNotExistsAndCountryExistsAndHasNotHeadquarter_thenThrowHeadSwiftCodeNotFoundException() {
        SwiftCodeItem branch = swiftCodeData.toBuilder()
            .swiftCode("ABCDEFGHABC")
            .headquarter(false)
            .build();
        
        when(swiftCodesRepository.findById(branch.getSwiftCode())).thenReturn(Optional.empty());
        when(countriesRepository.findById(branch.getCountryISO2().getCountryISO2())).thenReturn(Optional.of(countryPL));
        when(swiftCodesRepository.save(branch)).thenReturn(branch);
        when(swiftCodesRepository.findById(swiftCodeData.getSwiftCode())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> swiftCodesService.addSwiftCodeData(branch))
            .isInstanceOf(HeadSwiftCodeNotFoundException.class);
    }
    
    @Test
    public void whenGetSwiftCodesByCountryISO2AndCountryExists_thenReturnSwiftCodeResponses() {
        when(countriesRepository.findById(countryPL.getCountryISO2())).thenReturn(Optional.of(countryPL));
        when(swiftCodesRepository.findAllByCountryISO2(countryPL)).thenReturn(List.of(swiftCodeResponse));
        
        assertThatCode(() -> {
            var swiftCodes = swiftCodesService.getSwiftCodesDataByCountryISO2(countryPL.getCountryISO2());
            assertThat(swiftCodes.getSwiftCodes().size()).isEqualTo(1);
            assertThat(swiftCodes.getSwiftCodes()).allSatisfy(bank -> assertThat(bank.getCountryISO2()).isEqualTo(countryPL.getCountryISO2()));
        }).doesNotThrowAnyException();
    }
    
    @Test
    public void whenGetSwiftCodesByCountryISO2AndCountryDoesNotExists_thenThrowCountryNotExistsException() {
        when(countriesRepository.findById(countryPL.getCountryISO2())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> swiftCodesService.getSwiftCodesDataByCountryISO2(countryPL.getCountryISO2()))
            .isInstanceOf(CountryNotExistsException.class);
    }
    
    @Test
    public void whenDeleteSwiftCodeDataAndSwiftCodeDataExists_thenDeleteSwiftCodeData() {
        when(swiftCodesRepository.findById(swiftCodeData.getSwiftCode())).thenReturn(Optional.of(swiftCodeData));
        
        assertThatCode(() -> swiftCodesService.deleteSwiftCodeData(swiftCodeData.getSwiftCode())).doesNotThrowAnyException();
    }
    
    @Test
    public void whenDeleteSwiftCodeDataAndSwiftCodeDataDoesNotExists_thenThrowSwiftCodeNotFoundException() {
        when(swiftCodesRepository.findById(swiftCodeData.getSwiftCode())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> swiftCodesService.deleteSwiftCodeData(swiftCodeData.getSwiftCode()))
            .isInstanceOf(SwiftCodeNotFoundException.class);
    }
    
    @Test
    public void whenDeleteSwiftCodeDataAndSwiftCodeHasBranches_thenThrowChildSwiftCodesFoundException() {
        SwiftCodeItem branch = swiftCodeData.toBuilder()
            .swiftCode("ABCDEFGHABC")
            .headquarter(false)
            .build();
        
        swiftCodeData.getBranches().add(branch);
        
        when(swiftCodesRepository.findById(swiftCodeData.getSwiftCode())).thenReturn(Optional.of(swiftCodeData));
        
        assertThatThrownBy(() -> swiftCodesService.deleteSwiftCodeData(swiftCodeData.getSwiftCode()))
            .isInstanceOf(ChildSwiftCodesFoundException.class);
    }
}
