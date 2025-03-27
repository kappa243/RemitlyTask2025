package io.github.kappa243.remitly2025.controllers;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.kappa243.remitly2025.model.BankItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

@Projection(name = "bankResponse", types = {BankItem.class})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface BankResponse extends ReducedBankResponse {
    
    @Value("#{target.countryCode.getCountryName()}")
    String getCountryName();
    
    List<ReducedBankResponse> getBranches();
}
