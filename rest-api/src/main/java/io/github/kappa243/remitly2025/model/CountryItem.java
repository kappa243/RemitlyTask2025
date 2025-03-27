package io.github.kappa243.remitly2025.model;

import io.github.kappa243.remitly2025.model.validators.CountryCodeValidator;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "countries")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryItem {
    
    @Id
    @CountryCodeValidator
    private String countryCode;
    
    @NotEmpty
    private String countryName;
    
}
