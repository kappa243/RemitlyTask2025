package io.github.kappa243.remitly2025;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kappa243.remitly2025.controllers.BankResponse;
import io.github.kappa243.remitly2025.controllers.SwiftCodesController;
import io.github.kappa243.remitly2025.exceptions.BankNotFoundException;
import io.github.kappa243.remitly2025.model.BankItem;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.services.SwiftCodesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SwiftCodesController.class)
public class SwiftCodesControllerTests {
    
    String URI = "http://localhost";
    String PATH = "/v1/swift-codes";
    
    @MockitoBean
    private SwiftCodesService swiftCodesService;
    
    @Autowired
    MockMvc mockMvc;
    
    @Autowired
    ObjectMapper objectMapper;
    
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
    
}
