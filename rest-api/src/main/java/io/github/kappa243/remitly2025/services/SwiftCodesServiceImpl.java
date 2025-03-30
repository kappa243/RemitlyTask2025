package io.github.kappa243.remitly2025.services;

import io.github.kappa243.remitly2025.controllers.BankResponse;
import io.github.kappa243.remitly2025.controllers.CountryBanksResponse;
import io.github.kappa243.remitly2025.exceptions.BankAlreadyExistsException;
import io.github.kappa243.remitly2025.exceptions.BankNotFoundException;
import io.github.kappa243.remitly2025.exceptions.ChildBranchesFoundException;
import io.github.kappa243.remitly2025.exceptions.CountryNotExistsException;
import io.github.kappa243.remitly2025.exceptions.HeadBankNotFoundException;
import io.github.kappa243.remitly2025.model.BankItem;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.repositories.BanksRepository;
import io.github.kappa243.remitly2025.repositories.CountriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SwiftCodesServiceImpl implements SwiftCodesService {
    
    private final BanksRepository banksRepository;
    private final CountriesRepository countriesRepository;
    
    private final ProjectionFactory projectionFactory;
    
    
    @Override
    public BankResponse getBankBySwiftCode(String swiftCode) throws BankNotFoundException {
        Optional<BankResponse> response = banksRepository.findBySwiftCode(swiftCode);
        
        if (response.isEmpty())
            throw new BankNotFoundException();
        
        return response.get();
    }
    
    @Override
    public BankResponse addBank(BankItem bank) throws BankAlreadyExistsException, HeadBankNotFoundException {
        // check if bank already exists
        Optional<BankItem> existingBank = banksRepository.findById(bank.getSwiftCode());
        
        if (existingBank.isPresent())
            throw new BankAlreadyExistsException();
        
        // check if country exists
        Optional<CountryItem> country = countriesRepository.findById(bank.getCountryISO2().getCountryISO2());
        
        if (country.isEmpty())
            countriesRepository.save(bank.getCountryISO2());
        
        // country requirements were not provided in task;
        // we assume that country code is unique and is final after creation (dict)
        
        BankItem createdBank;
        
        if (!bank.isHeadquarter()) {
            String headSwiftCode = bank.getSwiftCode().substring(0, 8) + "XXX";
            Optional<BankItem> headBank = banksRepository.findById(headSwiftCode);
            
            if (headBank.isEmpty()) {
                throw new HeadBankNotFoundException();
            }
            
            headBank.get().getBranches().add(bank);
            
            createdBank = banksRepository.save(bank);
            banksRepository.save(headBank.get());
        } else {
            if (bank.getBranches() == null) {
                bank.setBranches(Collections.emptyList());
            }
            
            createdBank = banksRepository.save(bank);
        }
        
        return projectionFactory.createProjection(BankResponse.class, createdBank);
    }
    
    
    private CountryItem getCountryByCountryISO2(String countryISO2) throws CountryNotExistsException {
        Optional<CountryItem> countryItem = countriesRepository.findById(countryISO2);
        
        if (countryItem.isEmpty())
            throw new CountryNotExistsException();
        
        return countryItem.get();
    }
    
    @Override
    public CountryBanksResponse getBanksByCountryISO2(String countryISO2) throws CountryNotExistsException {
        CountryItem countryItem = getCountryByCountryISO2(countryISO2);
        
        return CountryBanksResponse.builder()
            .countryISO2(countryItem.getCountryISO2())
            .countryName(countryItem.getCountryName())
            .swiftCodes(banksRepository.findAllByCountryISO2_CountryISO2(countryItem.getCountryISO2()))
            .build();
    }
    
    @Override
    public void deleteBank(String swiftCode) throws BankNotFoundException, ChildBranchesFoundException {
        Optional<BankItem> bank = banksRepository.findById(swiftCode);
        
        if (bank.isEmpty())
            throw new BankNotFoundException();
        
        BankItem bankItem = bank.get();
        
        if (bankItem.isHeadquarter() && !bankItem.getBranches().isEmpty()) {
            throw new ChildBranchesFoundException();
        }
        
        banksRepository.delete(bank.get());
    }
    
}
