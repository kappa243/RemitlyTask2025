package io.github.kappa243.remitly2025.controllers;

import io.github.kappa243.remitly2025.exceptions.ChildSwiftCodesFoundException;
import io.github.kappa243.remitly2025.exceptions.CountryNotExistsException;
import io.github.kappa243.remitly2025.exceptions.HeadSwiftCodeNotFoundException;
import io.github.kappa243.remitly2025.exceptions.SwiftCodeAlreadyExistsException;
import io.github.kappa243.remitly2025.exceptions.SwiftCodeNotFoundException;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.model.SwiftCodeItem;
import io.github.kappa243.remitly2025.model.validators.CountryCode;
import io.github.kappa243.remitly2025.model.validators.SwiftCode;
import io.github.kappa243.remitly2025.services.SwiftCodesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/swift-codes")
@Validated
public class SwiftCodesController {
    
    private final SwiftCodesService swiftCodesService;
    
    @GetMapping("/{swiftCode}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SwiftCodeResponse getBankData(@PathVariable @SwiftCode String swiftCode) throws SwiftCodeNotFoundException {
        return swiftCodesService.getSwiftCodeDataBySwiftCode(swiftCode);
    }
    
    @GetMapping("/country/{countryISO2code}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public CountrySwiftCodesResponse getBanksByCountryISO2(@PathVariable @CountryCode String countryISO2code) throws CountryNotExistsException {
        return swiftCodesService.getSwiftCodesDataByCountryISO2(countryISO2code);
    }
    
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Map<String, String>> addBank(@Valid @RequestBody SwiftCodeRequest swiftCodeRequest) throws HeadSwiftCodeNotFoundException, SwiftCodeAlreadyExistsException {
        // map request to items
        CountryItem countryItem = CountryItem.builder()
            .countryISO2(swiftCodeRequest.getCountryISO2())
            .countryName(swiftCodeRequest.getCountryName())
            .build();
        
        SwiftCodeItem swiftCodeItem = SwiftCodeItem.builder()
            .swiftCode(swiftCodeRequest.getSwiftCode())
            .bankName(swiftCodeRequest.getBankName())
            .address(swiftCodeRequest.getAddress())
            .headquarter(swiftCodeRequest.isHeadquarter())
            .countryISO2(countryItem)
            .build();
        
        swiftCodesService.addSwiftCodeData(swiftCodeItem);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("message", "ok"));
    }
    
    @DeleteMapping("/{swiftCode}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Map<String, String>> deleteBank(@PathVariable @SwiftCode String swiftCode) throws SwiftCodeNotFoundException, ChildSwiftCodesFoundException {
        swiftCodesService.deleteSwiftCodeData(swiftCode);
        
        return ResponseEntity.status(HttpStatus.OK)
            .body(Map.of("message", "ok"));
    }
    
}
