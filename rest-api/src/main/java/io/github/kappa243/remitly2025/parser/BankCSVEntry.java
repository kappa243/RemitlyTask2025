package io.github.kappa243.remitly2025.parser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankCSVEntry {
    @JsonProperty("SWIFT CODE")
    private String swiftCode;
    
    @JsonProperty("NAME")
    private String name;
    
    @JsonProperty("ADDRESS")
    private String address;
    
    @JsonProperty("COUNTRY NAME")
    private String countryName;
    
    @JsonProperty("COUNTRY ISO2 CODE")
    private String countryISO2;
    
}
