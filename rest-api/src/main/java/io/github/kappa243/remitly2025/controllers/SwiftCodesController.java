package io.github.kappa243.remitly2025.controllers;

import io.github.kappa243.remitly2025.exceptions.BankAlreadyExistsException;
import io.github.kappa243.remitly2025.exceptions.BankNotFoundException;
import io.github.kappa243.remitly2025.exceptions.ChildBranchesFoundException;
import io.github.kappa243.remitly2025.exceptions.CountryNotExistsException;
import io.github.kappa243.remitly2025.exceptions.HeadBankNotFoundException;
import io.github.kappa243.remitly2025.model.BankItem;
import io.github.kappa243.remitly2025.model.CountryItem;
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
    public BankResponse getBankData(@PathVariable @SwiftCode String swiftCode) throws BankNotFoundException {
        return swiftCodesService.getBankBySwiftCode(swiftCode);
    }
    
    @GetMapping("/country/{countryISO2code}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public CountryBanksResponse getBanksByCountryISO2(@PathVariable @CountryCode String countryISO2code) throws CountryNotExistsException {
        return swiftCodesService.getBanksByCountryISO2(countryISO2code);
    }
    
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Map<String, String>> addBank(@Valid @RequestBody BankRequest bankRequest) throws HeadBankNotFoundException, BankAlreadyExistsException {
        // map request to items
        CountryItem countryItem = CountryItem.builder()
            .countryISO2(bankRequest.getCountryISO2())
            .countryName(bankRequest.getCountryName())
            .build();
        
        BankItem bankItem = BankItem.builder()
            .swiftCode(bankRequest.getSwiftCode())
            .name(bankRequest.getName())
            .address(bankRequest.getAddress())
            .headquarter(bankRequest.isHeadquarter())
            .countryISO2(countryItem)
            .build();
        
        swiftCodesService.addBank(bankItem);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("message", "ok"));
    }
    
    @DeleteMapping("/{swiftCode}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Map<String, String>> deleteBank(@PathVariable @SwiftCode String swiftCode) throws BankNotFoundException, ChildBranchesFoundException {
        swiftCodesService.deleteBank(swiftCode);
        
        return ResponseEntity.status(HttpStatus.OK)
            .body(Map.of("message", "ok"));
    }
    
}
