package io.github.kappa243.remitly2025.controllers;

import io.github.kappa243.remitly2025.model.BankItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;


@Projection(name = "reducedBankResponse", types = {BankItem.class})
public interface ReducedBankResponse {
    String getSwiftCode();
    
    String getName();
    
    String getAddress();
    
    boolean getisHeadquarter();
    
    @Value("#{target.countryCode.getCountryCode()}")
    String getCountryCode();
    
}
