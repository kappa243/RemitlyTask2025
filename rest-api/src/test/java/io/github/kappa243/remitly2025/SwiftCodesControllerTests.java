package io.github.kappa243.remitly2025;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kappa243.remitly2025.controllers.CountrySwiftCodesResponse;
import io.github.kappa243.remitly2025.controllers.SwiftCodeRequest;
import io.github.kappa243.remitly2025.controllers.SwiftCodeResponse;
import io.github.kappa243.remitly2025.controllers.SwiftCodesController;
import io.github.kappa243.remitly2025.exceptions.ChildSwiftCodesFoundException;
import io.github.kappa243.remitly2025.exceptions.CountryNotExistsException;
import io.github.kappa243.remitly2025.exceptions.HeadSwiftCodeNotFoundException;
import io.github.kappa243.remitly2025.exceptions.SwiftCodeAlreadyExistsException;
import io.github.kappa243.remitly2025.exceptions.SwiftCodeNotFoundException;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.model.SwiftCodeItem;
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
    SwiftCodeItem swiftCodeData = SwiftCodeItem.builder()
        .swiftCode("ABCDEFGHXXX")
        .bankName("MAIN STREET BANK")
        .address("1234 Main St")
        .countryISO2(countryPL)
        .headquarter(true)
        .build();
    
    SwiftCodeRequest swiftCodeRequest = SwiftCodeRequest.builder()
        .swiftCode(swiftCodeData.getSwiftCode())
        .bankName(swiftCodeData.getBankName())
        .address(swiftCodeData.getAddress())
        .countryISO2(countryPL.getCountryISO2())
        .countryName(countryPL.getCountryName())
        .headquarter(swiftCodeData.isHeadquarter())
        .build();
    
    SwiftCodeResponse swiftCodeResponse;
    
    @BeforeEach
    public void beforeEach() {
        swiftCodeResponse = projectionFactory.createProjection(SwiftCodeResponse.class, swiftCodeData);
    }
    
    @Test
    public void whenGetCode_thenOk() throws Exception {
        when(swiftCodesService.getSwiftCodeDataBySwiftCode(swiftCodeData.getSwiftCode())).thenReturn(swiftCodeResponse);
        
        mockMvc.perform(get(PATH + "/{swiftCode}", swiftCodeData.getSwiftCode()))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(swiftCodeResponse)));
    }
    
    @Test
    public void whenGetNonExistentCode_thenNotFound() throws Exception {
        String swiftCode = "ABCDEFGHIJK";
        when(swiftCodesService.getSwiftCodeDataBySwiftCode(swiftCode)).thenThrow(new SwiftCodeNotFoundException());
        
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
    public void whenPostSwiftCodeData_thenCreated() throws Exception {
        when(swiftCodesService.addSwiftCodeData(swiftCodeData)).thenReturn(swiftCodeResponse);
        
        mockMvc.perform(post(PATH + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(swiftCodeRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().string(containsString("ok")));
    }
    
    @Test
    public void whenPostSwiftCodeDataAndInvalidRequest_thenBadRequest() throws Exception {
        SwiftCodeRequest invalidSwiftCodeRequest = swiftCodeRequest.toBuilder()
            .swiftCode("AAA")
            .bankName("Main street bank")
            .build();
        
        mockMvc.perform(post(PATH + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidSwiftCodeRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Validation Error")))
            .andExpect(content().string(containsString("Invalid code length")))
            .andExpect(content().string(containsString("must be uppercase")));
    }
    
    @Test
    public void whenPostSwiftCodeRequestAndInvalidHeadquarterPattern_thenBadRequest() throws Exception {
        SwiftCodeRequest invalidSwiftCodeRequest = swiftCodeRequest.toBuilder()
            .swiftCode("ABCDEFGHABC")
            .build();
        
        mockMvc.perform(post(PATH + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidSwiftCodeRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Validation Error")))
            .andExpect(content().string(containsString("SWIFT code does not match")));
    }
    
    @Test
    public void whenPostSwiftCodeRequestAndBankExists_thenConflict() throws Exception {
        when(swiftCodesService.addSwiftCodeData(swiftCodeData)).thenThrow(new SwiftCodeAlreadyExistsException());
        
        mockMvc.perform(post(PATH + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(swiftCodeRequest)))
            .andExpect(status().isConflict());
    }
    
    @Test
    public void whenPostSwiftCodeRequestAndHeadquarterNotFound_thenConflict() throws Exception {
        SwiftCodeRequest branchSwiftCodeRequest = swiftCodeRequest.toBuilder()
            .swiftCode(swiftCodeRequest.getSwiftCode().substring(0, 8) + "ABC")
            .headquarter(false)
            .build();
        
        SwiftCodeItem branchSwiftCodeData = swiftCodeData.toBuilder()
            .swiftCode(branchSwiftCodeRequest.getSwiftCode())
            .headquarter(false)
            .build();
        
        when(swiftCodesService.addSwiftCodeData(branchSwiftCodeData)).thenReturn(swiftCodeResponse);
        
        when(swiftCodesService.addSwiftCodeData(branchSwiftCodeData)).thenThrow(new HeadSwiftCodeNotFoundException());
        
        mockMvc.perform(post(PATH + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(branchSwiftCodeRequest)))
            .andExpect(status().isConflict());
    }
    
    @Test
    public void whenGetSwiftCodesByCountryISO2_thenOk() throws Exception {
        String countryISO2 = countryPL.getCountryISO2();
        
        CountrySwiftCodesResponse countrySwiftCodesResponse = CountrySwiftCodesResponse.builder()
            .countryISO2(countryISO2)
            .countryName(countryPL.getCountryName())
            .swiftCodes(List.of(swiftCodeResponse))
            .build();
        
        when(swiftCodesService.getSwiftCodesDataByCountryISO2(countryISO2)).thenReturn(countrySwiftCodesResponse);
        
        mockMvc.perform(get(PATH + "/country/{countryISO2code}", countryISO2))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(countrySwiftCodesResponse)));
    }
    
    @Test
    public void whenGetSwiftCodesByCountryISO2AndCountryDoesNotExists_thenNotFound() throws Exception {
        String countryISO2 = "PL";
        when(swiftCodesService.getSwiftCodesDataByCountryISO2(countryISO2)).thenThrow(new CountryNotExistsException());
        
        mockMvc.perform(get(PATH + "/country/{countryISO2code}", countryISO2))
            .andExpect(status().isNotFound());
    }
    
    @Test
    public void whenDeleteCode_thenOk() throws Exception {
        String swiftCode = swiftCodeData.getSwiftCode();
        
        mockMvc.perform(delete(PATH + "/{swiftCode}", swiftCode))
            .andExpect(status().isOk());
    }
    
    @Test
    public void whenDeleteCodeAndSwiftCodeDataDoesNotExists_thenNotFound() throws Exception {
        String swiftCode = "ABCDEFGHIJK";
        doThrow(new SwiftCodeNotFoundException()).when(swiftCodesService).deleteSwiftCodeData(swiftCode);
        
        mockMvc.perform(delete(PATH + "/{swiftCode}", swiftCode))
            .andExpect(status().isNotFound());
    }
    
    @Test
    public void whenDeleteCodeAndChildSwiftCodesFound_thenConflict() throws Exception {
        String swiftCode = swiftCodeData.getSwiftCode();
        doThrow(new ChildSwiftCodesFoundException()).when(swiftCodesService).deleteSwiftCodeData(swiftCode);
        
        mockMvc.perform(delete(PATH + "/{swiftCode}", swiftCode))
            .andExpect(status().isConflict());
    }
}
