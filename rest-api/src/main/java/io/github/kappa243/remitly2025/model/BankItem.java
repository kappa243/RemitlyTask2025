package io.github.kappa243.remitly2025.model;

import io.github.kappa243.remitly2025.model.validators.SwiftCodeValidator;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "banks")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankItem {
    
    @Id
    @SwiftCodeValidator
    private String swiftCode;
    
    @NotEmpty
    private String name;
    
    @NotEmpty
    private String address;
    
    @NotEmpty
    private boolean isHeadquarter;
    
    @DBRef
    @Indexed
    @NotEmpty
    private CountryItem countryCode;
    
    @DBRef
    private List<BankItem> branches;
    
    public BankItem(String swiftCode, String name, String address, boolean isHeadquarter, CountryItem countryCode) {
        this.swiftCode = swiftCode;
        this.name = name;
        this.address = address;
        this.isHeadquarter = isHeadquarter;
        this.countryCode = countryCode;
    }
    
}
