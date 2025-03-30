package io.github.kappa243.remitly2025.services;

import io.github.kappa243.remitly2025.controllers.CountrySwiftCodesResponse;
import io.github.kappa243.remitly2025.controllers.SwiftCodeResponse;
import io.github.kappa243.remitly2025.exceptions.ChildSwiftCodesFoundException;
import io.github.kappa243.remitly2025.exceptions.CountryNotExistsException;
import io.github.kappa243.remitly2025.exceptions.HeadSwiftCodeNotFoundException;
import io.github.kappa243.remitly2025.exceptions.SwiftCodeAlreadyExistsException;
import io.github.kappa243.remitly2025.exceptions.SwiftCodeNotFoundException;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.model.SwiftCodeItem;
import io.github.kappa243.remitly2025.repositories.CountriesRepository;
import io.github.kappa243.remitly2025.repositories.SwiftCodesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SwiftCodesServiceImpl implements SwiftCodesService {
    
    private final SwiftCodesRepository swiftCodesRepository;
    private final CountriesRepository countriesRepository;
    
    private final ProjectionFactory projectionFactory;
    
    
    @Override
    public SwiftCodeResponse getSwiftCodeDataBySwiftCode(String swiftCode) throws SwiftCodeNotFoundException {
        return swiftCodesRepository.findBySwiftCode(swiftCode)
            .orElseThrow(SwiftCodeNotFoundException::new);
    }
    
    @Override
    public SwiftCodeResponse addSwiftCodeData(SwiftCodeItem swiftCodeItem) throws SwiftCodeAlreadyExistsException, HeadSwiftCodeNotFoundException {
        // check if bank already exists
        swiftCodesRepository.findById(swiftCodeItem.getSwiftCode()).ifPresent(sc -> {
            throw new SwiftCodeAlreadyExistsException();
        });
        
        // check if country exists
        Optional<CountryItem> country = countriesRepository.findById(swiftCodeItem.getCountryISO2().getCountryISO2());
        if (country.isEmpty())
            countriesRepository.save(swiftCodeItem.getCountryISO2());
        
        // country requirements were not provided in task;
        // we assume that country code is unique and is final after creation (dict)
        
        SwiftCodeItem createdSwiftCodeData;
        
        if (!swiftCodeItem.isHeadquarter()) {
            String headSwiftCode = swiftCodeItem.getSwiftCode().substring(0, 8) + "XXX";
            Optional<SwiftCodeItem> headSwiftCodeData = swiftCodesRepository.findById(headSwiftCode);
            
            if (headSwiftCodeData.isEmpty()) {
                throw new HeadSwiftCodeNotFoundException();
            }
            
            headSwiftCodeData.get().getBranches().add(swiftCodeItem);
            
            createdSwiftCodeData = swiftCodesRepository.save(swiftCodeItem);
            swiftCodesRepository.save(headSwiftCodeData.get());
        } else {
            if (swiftCodeItem.getBranches() == null) {
                swiftCodeItem.setBranches(Collections.emptyList());
            }
            
            createdSwiftCodeData = swiftCodesRepository.save(swiftCodeItem);
        }
        
        return projectionFactory.createProjection(SwiftCodeResponse.class, createdSwiftCodeData);
    }
    
    
    private CountryItem getCountryByCountryISO2(String countryISO2) throws CountryNotExistsException {
        return countriesRepository.findById(countryISO2)
            .orElseThrow(CountryNotExistsException::new);
    }
    
    @Override
    public CountrySwiftCodesResponse getSwiftCodesDataByCountryISO2(String countryISO2) throws CountryNotExistsException {
        CountryItem countryData = getCountryByCountryISO2(countryISO2);
        
        return CountrySwiftCodesResponse.builder()
            .countryISO2(countryData.getCountryISO2())
            .countryName(countryData.getCountryName())
            .swiftCodes(swiftCodesRepository.findAllByCountryISO2_CountryISO2(countryData.getCountryISO2()))
            .build();
    }
    
    @Override
    public void deleteSwiftCodeData(String swiftCode) throws SwiftCodeNotFoundException, ChildSwiftCodesFoundException {
        SwiftCodeItem swiftCodeData = swiftCodesRepository.findById(swiftCode)
            .orElseThrow(SwiftCodeNotFoundException::new);
        
        if (swiftCodeData.isHeadquarter() && !swiftCodeData.getBranches().isEmpty()) {
            throw new ChildSwiftCodesFoundException();
        }
        
        swiftCodesRepository.delete(swiftCodeData);
    }
    
}
