package io.github.kappa243.remitly2025.services;

import io.github.kappa243.remitly2025.controllers.BankResponse;
import io.github.kappa243.remitly2025.exceptions.BankNotFoundException;

public interface SwiftCodesService {
    
    BankResponse getBankBySwiftCode(String swiftCode) throws BankNotFoundException;
}
