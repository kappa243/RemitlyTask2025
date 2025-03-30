package io.github.kappa243.remitly2025.services;

import io.github.kappa243.remitly2025.controllers.CountrySwiftCodesResponse;
import io.github.kappa243.remitly2025.controllers.SwiftCodeResponse;
import io.github.kappa243.remitly2025.exceptions.ChildSwiftCodesFoundException;
import io.github.kappa243.remitly2025.exceptions.CountryNotExistsException;
import io.github.kappa243.remitly2025.exceptions.HeadSwiftCodeNotFoundException;
import io.github.kappa243.remitly2025.exceptions.SwiftCodeAlreadyExistsException;
import io.github.kappa243.remitly2025.exceptions.SwiftCodeNotFoundException;
import io.github.kappa243.remitly2025.model.SwiftCodeItem;

public interface SwiftCodesService {
    
    SwiftCodeResponse getSwiftCodeDataBySwiftCode(String swiftCode) throws SwiftCodeNotFoundException;
    
    SwiftCodeResponse addSwiftCodeData(SwiftCodeItem swiftCodeItem) throws SwiftCodeAlreadyExistsException, HeadSwiftCodeNotFoundException;
    
    CountrySwiftCodesResponse getSwiftCodesDataByCountryISO2(String countryISO2) throws CountryNotExistsException;
    
    void deleteSwiftCodeData(String swiftCode) throws SwiftCodeNotFoundException, ChildSwiftCodesFoundException;
}
