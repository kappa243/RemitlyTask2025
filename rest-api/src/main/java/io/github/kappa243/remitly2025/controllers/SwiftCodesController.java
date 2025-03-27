package io.github.kappa243.remitly2025.controllers;

import io.github.kappa243.remitly2025.exceptions.BankNotFoundException;
import io.github.kappa243.remitly2025.model.validators.SwiftCodeValidator;
import io.github.kappa243.remitly2025.services.SwiftCodesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/swift-codes")
@Validated
public class SwiftCodesController {
    
    private final SwiftCodesService swiftCodesService;
    
    @GetMapping("/{swiftCode}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public BankResponse getBankData(@PathVariable @SwiftCodeValidator String swiftCode) throws BankNotFoundException {
        return swiftCodesService.getBankBySwiftCode(swiftCode);
    }
    
}
