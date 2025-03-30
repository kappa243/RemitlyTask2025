package io.github.kappa243.remitly2025.controllers;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.kappa243.remitly2025.model.SwiftCodeItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

@Projection(name = "swiftCodeResponse", types = {SwiftCodeItem.class})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface SwiftCodeResponse extends ReducedSwiftCodeResponse {
    
    @Value("#{target.countryISO2.getCountryName()}")
    String getCountryName();
    
    List<ReducedSwiftCodeResponse> getBranches();
}
