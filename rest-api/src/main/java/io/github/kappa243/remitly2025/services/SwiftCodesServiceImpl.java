package io.github.kappa243.remitly2025.services;

import io.github.kappa243.remitly2025.controllers.BankResponse;
import io.github.kappa243.remitly2025.exceptions.BankNotFoundException;
import io.github.kappa243.remitly2025.repositories.BanksRepository;
import io.github.kappa243.remitly2025.repositories.CountriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SwiftCodesServiceImpl implements SwiftCodesService {
    
    private final BanksRepository banksRepository;
    private final CountriesRepository countriesRepository;
    
    @Override
    public BankResponse getBankBySwiftCode(String swiftCode) throws BankNotFoundException {
        Optional<BankResponse> response = banksRepository.findBySwiftCode(swiftCode);
        
        if (response.isEmpty()) {
            throw new BankNotFoundException();
        }
        
        return response.get();
    }
}
