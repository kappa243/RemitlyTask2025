package io.github.kappa243.remitly2025.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kappa243.remitly2025.model.BankItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;


@Projection(name = "reducedBankResponse", types = {BankItem.class})
public interface ReducedBankResponse {
    String getSwiftCode();
    
    String getName();
    
    String getAddress();
    
    @JsonProperty("isHeadquarter")
    boolean getHeadquarter();
    
    @Value("#{target.countryCode.getCountryCode()}")
    String getCountryCode();
    
}
