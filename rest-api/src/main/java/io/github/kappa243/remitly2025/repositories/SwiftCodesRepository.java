package io.github.kappa243.remitly2025.repositories;

import io.github.kappa243.remitly2025.controllers.ReducedSwiftCodeResponse;
import io.github.kappa243.remitly2025.controllers.SwiftCodeResponse;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.model.SwiftCodeItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SwiftCodesRepository extends MongoRepository<SwiftCodeItem, String> {
    
    Optional<SwiftCodeResponse> findBySwiftCode(String swiftCode);
    
    List<ReducedSwiftCodeResponse> findAllByCountryISO2(CountryItem countryISO2);
}
