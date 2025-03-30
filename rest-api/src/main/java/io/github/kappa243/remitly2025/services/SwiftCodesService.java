package io.github.kappa243.remitly2025.services;

import io.github.kappa243.remitly2025.controllers.BankResponse;
import io.github.kappa243.remitly2025.controllers.CountryBanksResponse;
import io.github.kappa243.remitly2025.exceptions.BankAlreadyExistsException;
import io.github.kappa243.remitly2025.exceptions.BankNotFoundException;
import io.github.kappa243.remitly2025.exceptions.ChildBranchesFoundException;
import io.github.kappa243.remitly2025.exceptions.CountryNotExistsException;
import io.github.kappa243.remitly2025.exceptions.HeadBankNotFoundException;
import io.github.kappa243.remitly2025.model.BankItem;

public interface SwiftCodesService {
    
    BankResponse getBankBySwiftCode(String swiftCode) throws BankNotFoundException;
    
    BankResponse addBank(BankItem bank) throws BankAlreadyExistsException, HeadBankNotFoundException;
    
    CountryBanksResponse getBanksByCountryISO2(String countryISO2) throws CountryNotExistsException;
    
    void deleteBank(String swiftCode) throws BankNotFoundException, ChildBranchesFoundException;
}
