package io.github.kappa243.remitly2025.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kappa243.remitly2025.model.SwiftCodeItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;


@Projection(name = "reducedSwiftCodeResponse", types = {SwiftCodeItem.class})
public interface ReducedSwiftCodeResponse {
    String getSwiftCode();
    
    String getBankName();
    
    String getAddress();
    
    @JsonProperty("isHeadquarter")
    boolean getHeadquarter();
    
    @Value("#{target.countryISO2.getCountryISO2()}")
    String getCountryISO2();
    
}
