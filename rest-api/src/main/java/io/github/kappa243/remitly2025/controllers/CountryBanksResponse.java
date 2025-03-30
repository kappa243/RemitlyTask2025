package io.github.kappa243.remitly2025.controllers;

import io.github.kappa243.remitly2025.model.validators.CountryCode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryBanksResponse {
    
    @NotEmpty
    @CountryCode
    private String countryISO2;
    
    @NotEmpty
    private String countryName;
    
    @NotNull
    private List<ReducedBankResponse> swiftCodes;
}
