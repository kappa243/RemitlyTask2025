package io.github.kappa243.remitly2025.parser;

import com.opencsv.bean.CsvBindByName;
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
public class BankCSVEntry {
    @CsvBindByName(column = "SWIFT CODE")
    private String swiftCode;
    
    @CsvBindByName(column = "NAME")
    private String name;
    
    @CsvBindByName(column = "ADDRESS")
    private String address;
    
    @CsvBindByName(column = "COUNTRY NAME")
    private String countryName;
    
    @CsvBindByName(column = "COUNTRY ISO2 CODE")
    private String countryCode;
    
}
