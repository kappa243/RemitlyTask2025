package io.github.kappa243.remitly2025.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kappa243.remitly2025.model.validators.SwiftCode;
import io.github.kappa243.remitly2025.model.validators.Uppercase;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "swiftcodes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class SwiftCodeItem {
    
    @Id
    @SwiftCode
    private String swiftCode;
    
    @NotEmpty
    @Uppercase
    private String bankName;
    
    @NotEmpty
    private String address;
    
    @NotNull
    @Field(name = "isHeadquarter")
    @JsonProperty("isHeadquarter")
    private boolean headquarter;
    
    @DBRef
    @Indexed
    @NotEmpty
    private CountryItem countryISO2;
    
    @DBRef
    @Setter
    private List<SwiftCodeItem> branches;
    
    public SwiftCodeItem(String swiftCode, String bankName, String address, boolean headquarter, CountryItem countryISO2) {
        this.swiftCode = swiftCode;
        this.bankName = bankName;
        this.address = address;
        this.headquarter = headquarter;
        this.countryISO2 = countryISO2;
    }
}
