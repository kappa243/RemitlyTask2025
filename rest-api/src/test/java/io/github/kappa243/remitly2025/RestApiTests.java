package io.github.kappa243.remitly2025;

import io.github.kappa243.remitly2025.controllers.BankRequest;
import io.github.kappa243.remitly2025.model.BankItem;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.repositories.BanksRepository;
import io.github.kappa243.remitly2025.repositories.CountriesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

public class RestApiTests extends BaseTestModule {
    
    @Autowired
    private BanksRepository banksRepository;
    
    @Autowired
    private CountriesRepository countriesRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    ObjectMapper objectMapper = new ObjectMapper();
    
    
    @BeforeEach
    public void fillDatabase() {
        if (mongoTemplate.collectionExists(BankItem.class) || mongoTemplate.collectionExists(CountryItem.class)) {
            // clear all data
            mongoTemplate.dropCollection(BankItem.class);
            mongoTemplate.dropCollection(CountryItem.class);
            
            mongoTemplate.indexOps(BankItem.class).ensureIndex(new Index().on("countryCode", Sort.Direction.ASC).named("countryCode_"));
        }
        
        var country_pl = new CountryItem("PL", "POLAND");
        
        countriesRepository.save(country_pl);
        
        var bank_child_a = new BankItem("BREXPLPWWRO", "MBANK S.A. (FORMERLY BRE BANK S.A.)", "PL. JANA PAWLA II 9  WROCLAW, DOLNOSLASKIE, 50-136", false, country_pl);
        var bank_child_b = new BankItem("BREXPLPWWAL", "MBANK S.A. (FORMERLY BRE BANK S.A.)", "SIENKIEWICZA 2  WALBRZYCH, DOLNOSLASKIE, 58-300", false, country_pl);
        var bank_head = new BankItem("BREXPLPWXXX", "MBANK S.A. (FORMERLY BRE BANK S.A.)", "UL. PROSTA 18  WARSZAWA, MAZOWIECKIE, 00-850", true, country_pl, List.of(bank_child_a, bank_child_b));
        
        banksRepository.save(bank_child_a);
        banksRepository.save(bank_child_b);
        banksRepository.save(bank_head);
    }
    
    
    String headSwiftCode = "BREXPLPWXXX";
    String branchSwiftCode = "BREXPLPWWAL";
    
    CountryItem countryPL = new CountryItem("PL", "POLAND");
    
    BankItem bankItem = BankItem.builder()
        .swiftCode("ABCDEFGHXXX")
        .name("MAIN STREET BANK")
        .address("1234 Main St")
        .countryCode(countryPL)
        .headquarter(true)
        .branches(new ArrayList<>())
        .build();
    
    BankRequest bankRequest = BankRequest.builder()
        .swiftCode(bankItem.getSwiftCode())
        .name(bankItem.getName())
        .address(bankItem.getAddress())
        .countryCode(countryPL.getCountryCode())
        .countryName(countryPL.getCountryName())
        .headquarter(bankItem.isHeadquarter())
        .build();
    
    //
    
    @Test
    public void whenGetHeadCode_thenOkAndDataIsCorrect() {
        when()
            .get("/{swiftCode}", headSwiftCode)
            .then()
            .statusCode(200)
            .log().all()
            .body("swiftCode", is(headSwiftCode))
            .body("name", is("MBANK S.A. (FORMERLY BRE BANK S.A.)"))
            .body("address", is("UL. PROSTA 18  WARSZAWA, MAZOWIECKIE, 00-850"))
            .body("countryCode", is("PL"))
            .body("countryName", is("POLAND"))
            .body("isHeadquarter", is(true))
            .body("branches.swiftCode", hasItem(branchSwiftCode));
    }
    
    @Test
    public void whenGetBranchCode_thenOk() {
        when()
            .get("/{swiftCode}", branchSwiftCode)
            .then()
            .statusCode(200);
    }
    
    @Test
    public void whenGetNonexistentCode_thenNotFound() {
        String swiftCode = "ABCDEFGHIJK";
        
        when()
            .get("/{swiftCode}", swiftCode)
            .then()
            .statusCode(404);
    }
    
    @Test
    public void whenGetInvalidCodeLength_thenBadRequest() {
        String swiftCode = "ABC";
        
        when()
            .get("/{swiftCode}", swiftCode)
            .then()
            .statusCode(400)
            .body(containsString("Validation Error"), containsString("Invalid code length"));
    }
    
    @Test
    public void whenGetInvalidCodePattern_thenBadRequest() {
        String swiftCode = "123DEFGHIJK";
        
        when()
            .get("/{swiftCode}", swiftCode)
            .then()
            .statusCode(400)
            .body(containsString("Validation Error"), containsString("Invalid SWIFT code pattern"));
    }
    
    @Test
    public void whenPostBankAndIsHeadquarter_thenOkAndDataIsCorrect() throws JsonProcessingException {
        given()
            .contentType("application/json")
            .body(objectMapper.writeValueAsString(bankRequest))
            .when()
            .post("/")
            .then()
            .statusCode(201)
            .body(containsString("ok"));
        
        when()
            .get("/{swiftCode}", bankRequest.getSwiftCode())
            .then()
            .statusCode(200)
            .log().all()
            .body("swiftCode", is(bankRequest.getSwiftCode()))
            .body("name", is(bankRequest.getName()))
            .body("address", is(bankRequest.getAddress()))
            .body("countryCode", is(bankRequest.getCountryCode()))
            .body("countryName", is(bankRequest.getCountryName()))
            .body("isHeadquarter", is(true));
    }
    
    @Test
    public void whenPostBankAndHasHeadquarter_thenOk() throws JsonProcessingException {
        BankRequest branchBankRequest = bankRequest.toBuilder()
            .swiftCode(headSwiftCode.substring(0, 8) + "ABC")
            .headquarter(false)
            .build();
        
        given()
            .contentType("application/json")
            .body(objectMapper.writeValueAsString(branchBankRequest))
            .when()
            .post("/")
            .then()
            .statusCode(201)
            .body(containsString("ok"));
        
        // check if in head branches there is added branch
        when()
            .get("/{swiftCode}", headSwiftCode)
            .then()
            .statusCode(200)
            .body("branches.swiftCode", hasItem(branchBankRequest.getSwiftCode()));
    }
    
    @Test
    public void whenPostBankAndHeadquarterNotExists_thenConflict() throws JsonProcessingException {
        BankRequest branchBankRequest = bankRequest.toBuilder()
            .swiftCode("NOTEXIST" + "ABC")
            .headquarter(false)
            .build();
        
        given()
            .contentType("application/json")
            .body(objectMapper.writeValueAsString(branchBankRequest))
            .when()
            .post("/")
            .then()
            .statusCode(409)
            .body(containsString("Headquarter bank does not exists"));
    }
    
    @Test
    public void whenPostBankAndBankExists_thenConflict() throws JsonProcessingException {
        BankRequest branchBankRequest = bankRequest.toBuilder()
            .swiftCode(branchSwiftCode)
            .headquarter(false)
            .build();
        
        given()
            .contentType("application/json")
            .body(objectMapper.writeValueAsString(branchBankRequest))
            .when()
            .post("/")
            .then()
            .statusCode(409)
            .body(containsString("Bank already exists"));
    }
}
