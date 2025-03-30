package io.github.kappa243.remitly2025.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kappa243.remitly2025.model.validators.CountryCode;
import io.github.kappa243.remitly2025.model.validators.HeadquarterMatch;
import io.github.kappa243.remitly2025.model.validators.SwiftCode;
import io.github.kappa243.remitly2025.model.validators.Uppercase;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@HeadquarterMatch
public class BankRequest {
    
    @NotEmpty
    @SwiftCode
    private String swiftCode;
    
    @NotEmpty
    @Uppercase
    private String name;
    
    @NotEmpty
    private String address;
    
    @NotNull
    @JsonProperty("isHeadquarter")
    private boolean headquarter;
    
    @NotEmpty
    @CountryCode
    private String countryISO2;
    
    @NotEmpty
    @Uppercase
    private String countryName;
}
