package io.github.kappa243.remitly2025.repositories;

import io.github.kappa243.remitly2025.controllers.BankResponse;
import io.github.kappa243.remitly2025.controllers.ReducedBankResponse;
import io.github.kappa243.remitly2025.model.BankItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BanksRepository extends MongoRepository<BankItem, String> {
    
    Optional<BankResponse> findBySwiftCode(String swiftCode);
    
    List<ReducedBankResponse> findAllByCountryISO2_CountryISO2(String countryISO2CountryISO2);
}
