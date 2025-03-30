package io.github.kappa243.remitly2025.services;

import io.github.kappa243.remitly2025.controllers.BankResponse;
import io.github.kappa243.remitly2025.exceptions.BankAlreadyExistsException;
import io.github.kappa243.remitly2025.exceptions.BankNotFoundException;
import io.github.kappa243.remitly2025.exceptions.HeadBankNotFoundException;
import io.github.kappa243.remitly2025.model.BankItem;
import jakarta.validation.Valid;

public interface SwiftCodesService {
    
    BankResponse getBankBySwiftCode(String swiftCode) throws BankNotFoundException;
    
    BankResponse addBank(@Valid BankItem bank) throws BankAlreadyExistsException, HeadBankNotFoundException;
}
