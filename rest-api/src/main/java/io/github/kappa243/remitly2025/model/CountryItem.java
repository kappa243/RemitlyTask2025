package io.github.kappa243.remitly2025.model;

import io.github.kappa243.remitly2025.model.validators.CountryCode;
import io.github.kappa243.remitly2025.model.validators.Uppercase;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "countries")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class CountryItem {
    
    @Id
    @CountryCode
    private String countryCode;
    
    @NotEmpty
    @Uppercase
    private String countryName;
    
}
