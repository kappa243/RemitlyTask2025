package io.github.kappa243.remitly2025;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kappa243.remitly2025.controllers.BankRequest;
import io.github.kappa243.remitly2025.controllers.BankResponse;
import io.github.kappa243.remitly2025.controllers.CountryBanksResponse;
import io.github.kappa243.remitly2025.controllers.SwiftCodesController;
import io.github.kappa243.remitly2025.exceptions.BankAlreadyExistsException;
import io.github.kappa243.remitly2025.exceptions.BankNotFoundException;
import io.github.kappa243.remitly2025.exceptions.ChildBranchesFoundException;
import io.github.kappa243.remitly2025.exceptions.CountryNotExistsException;
import io.github.kappa243.remitly2025.exceptions.HeadBankNotFoundException;
import io.github.kappa243.remitly2025.model.BankItem;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.services.SwiftCodesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SwiftCodesController.class)
@Import({ProjectionConfig.class})
public class SwiftCodesControllerTests {
    
    String URI = "http://localhost";
    String PATH = "/v1/swift-codes";
    
    @MockitoBean
    SwiftCodesService swiftCodesService;
    
    @Autowired
    MockMvc mockMvc;
    
    @Autowired
    ObjectMapper objectMapper;
    
    @Autowired
    ProjectionFactory projectionFactory;
    
    CountryItem countryPL = new CountryItem("PL", "POLAND");
    BankItem bankItem = BankItem.builder()
        .swiftCode("ABCDEFGHXXX")
        .name("MAIN STREET BANK")
        .address("1234 Main St")
        .countryISO2(countryPL)
        .headquarter(true)
        .build();
    
    BankRequest bankRequest = BankRequest.builder()
        .swiftCode(bankItem.getSwiftCode())
        .name(bankItem.getName())
        .address(bankItem.getAddress())
        .countryISO2(countryPL.getCountryISO2())
        .countryName(countryPL.getCountryName())
        .headquarter(bankItem.isHeadquarter())
        .build();
    
    BankResponse bankResponse;
    
    @BeforeEach
    public void beforeEach() {
        bankResponse = projectionFactory.createProjection(BankResponse.class, bankItem);
    }
    
    @Test
    public void whenGetCode_thenOk() throws Exception {
        when(swiftCodesService.getBankBySwiftCode(bankItem.getSwiftCode())).thenReturn(bankResponse);
        
        mockMvc.perform(get(PATH + "/{swiftCode}", bankItem.getSwiftCode()))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(bankResponse)));
    }
    
    @Test
    public void whenGetNonexistentCode_thenNotFound() throws Exception {
        String swiftCode = "ABCDEFGHIJK";
        when(swiftCodesService.getBankBySwiftCode(swiftCode)).thenThrow(new BankNotFoundException());
        
        mockMvc.perform(get(PATH + "/{swiftCode}", swiftCode))
            .andExpect(status().isNotFound());
    }
    
    @Test
    public void whenGetInvalidCodeLength_thenBadRequest() throws Exception {
        String swiftCode = "ABC";
        
        mockMvc.perform(get(PATH + "/{swiftCode}", swiftCode))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Validation Error")))
            .andExpect(content().string(containsString("Invalid code length")));
    }
    
    @Test
    public void whenGetInvalidCodePattern_thenBadRequest() throws Exception {
        String swiftCode = "123DEFGHIJK";
        
        mockMvc.perform(get(PATH + "/{swiftCode}", swiftCode))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Validation Error")))
            .andExpect(content().string(containsString("Invalid SWIFT code pattern")));
    }
    
    @Test
    public void whenPostBank_thenCreated() throws Exception {
        when(swiftCodesService.addBank(bankItem)).thenReturn(bankResponse);
        
        mockMvc.perform(post(PATH + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bankRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().string(containsString("ok")));
    }
    
    @Test
    public void whenPostBankAndInvalidRequest_thenBadRequest() throws Exception {
        BankRequest invalidBankRequest = bankRequest.toBuilder()
            .swiftCode("AAA")
            .name("Main street bank")
            .build();
        
        mockMvc.perform(post(PATH + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBankRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Validation Error")))
            .andExpect(content().string(containsString("Invalid code length")))
            .andExpect(content().string(containsString("must be uppercase")));
    }
    
    @Test
    public void whenPostBankAndInvalidHeadquarterPattern_thenBadRequest() throws Exception {
        BankRequest invalidBankRequest = bankRequest.toBuilder()
            .swiftCode("ABCDEFGHABC")
            .build();
        
        mockMvc.perform(post(PATH + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBankRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Validation Error")))
            .andExpect(content().string(containsString("SWIFT code does not match")));
    }
    
    @Test
    public void whenPostBankAndBankExists_thenConflict() throws Exception {
        when(swiftCodesService.addBank(bankItem)).thenThrow(new BankAlreadyExistsException());
        
        mockMvc.perform(post(PATH + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bankRequest)))
            .andExpect(status().isConflict());
    }
    
    @Test
    public void whenPostBankAndHeadquarterNotFound_thenConflict() throws Exception {
        BankRequest branchBankRequest = bankRequest.toBuilder()
            .swiftCode(bankRequest.getSwiftCode().substring(0, 8) + "ABC")
            .headquarter(false)
            .build();
        
        BankItem branchBankItem = bankItem.toBuilder()
            .swiftCode(branchBankRequest.getSwiftCode())
            .headquarter(false)
            .build();
        
        when(swiftCodesService.addBank(branchBankItem)).thenReturn(bankResponse);
        
        when(swiftCodesService.addBank(branchBankItem)).thenThrow(new HeadBankNotFoundException());
        
        mockMvc.perform(post(PATH + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(branchBankRequest)))
            .andExpect(status().isConflict());
    }
    
    @Test
    public void whenGetBanksByCountryISO2_thenOk() throws Exception {
        String countryISO2 = countryPL.getCountryISO2();
        
        CountryBanksResponse countryBanksResponse = CountryBanksResponse.builder()
            .countryISO2(countryISO2)
            .countryName(countryPL.getCountryName())
            .swiftCodes(List.of(bankResponse))
            .build();
        
        when(swiftCodesService.getBanksByCountryISO2(countryISO2)).thenReturn(countryBanksResponse);
        
        mockMvc.perform(get(PATH + "/country/{countryISO2code}", countryISO2))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(countryBanksResponse)));
    }
    
    @Test
    public void whenGetBanksByCountryISO2AndCountryDoesNotExists_thenNotFound() throws Exception {
        String countryISO2 = "PL";
        when(swiftCodesService.getBanksByCountryISO2(countryISO2)).thenThrow(new CountryNotExistsException());
        
        mockMvc.perform(get(PATH + "/country/{countryISO2code}", countryISO2))
            .andExpect(status().isNotFound());
    }
    
    @Test
    public void whenDeleteBank_thenOk() throws Exception {
        String swiftCode = bankItem.getSwiftCode();
        
        mockMvc.perform(delete(PATH + "/{swiftCode}", swiftCode))
            .andExpect(status().isOk());
    }
    
    @Test
    public void whenDeleteBankAndBankDoesNotExists_thenNotFound() throws Exception {
        String swiftCode = "ABCDEFGHIJK";
        doThrow(new BankNotFoundException()).when(swiftCodesService).deleteBank(swiftCode);
        
        mockMvc.perform(delete(PATH + "/{swiftCode}", swiftCode))
            .andExpect(status().isNotFound());
    }
    
    @Test
    public void whenDeleteBankAndChildBranchesFound_thenConflict() throws Exception {
        String swiftCode = bankItem.getSwiftCode();
        doThrow(new ChildBranchesFoundException()).when(swiftCodesService).deleteBank(swiftCode);
        
        mockMvc.perform(delete(PATH + "/{swiftCode}", swiftCode))
            .andExpect(status().isConflict());
    }
}
