package io.github.kappa243.remitly2025.repositories;

import io.github.kappa243.remitly2025.model.CountryItem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CountriesRepository extends MongoRepository<CountryItem, String> {
}
