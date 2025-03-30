package io.github.kappa243.remitly2025;

import io.github.kappa243.remitly2025.controllers.SwiftCodeRequest;
import io.github.kappa243.remitly2025.model.SwiftCodeItem;
import io.github.kappa243.remitly2025.model.CountryItem;
import io.github.kappa243.remitly2025.repositories.SwiftCodesRepository;
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
    private SwiftCodesRepository swiftCodesRepository;
    
    @Autowired
    private CountriesRepository countriesRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    ObjectMapper objectMapper = new ObjectMapper();
    
    
    @BeforeEach
    public void fillDatabase() {
        if (mongoTemplate.collectionExists(SwiftCodeItem.class) || mongoTemplate.collectionExists(CountryItem.class)) {
            // clear all data
            mongoTemplate.dropCollection(SwiftCodeItem.class);
            mongoTemplate.dropCollection(CountryItem.class);
            
            mongoTemplate.indexOps(SwiftCodeItem.class).ensureIndex(new Index().on("countryISO2", Sort.Direction.ASC).named("countryISO2_"));
        }
        
        var country = new CountryItem("PL", "POLAND");
        
        countriesRepository.save(country);
        
        var swiftCodeChildA = new SwiftCodeItem("BREXPLPWWRO", "MBANK S.A. (FORMERLY BRE BANK S.A.)", "PL. JANA PAWLA II 9  WROCLAW, DOLNOSLASKIE, 50-136", false, country);
        var swiftCodeChildN = new SwiftCodeItem("BREXPLPWWAL", "MBANK S.A. (FORMERLY BRE BANK S.A.)", "SIENKIEWICZA 2  WALBRZYCH, DOLNOSLASKIE, 58-300", false, country);
        var headSwiftCode = new SwiftCodeItem("BREXPLPWXXX", "MBANK S.A. (FORMERLY BRE BANK S.A.)", "UL. PROSTA 18  WARSZAWA, MAZOWIECKIE, 00-850", true, country, List.of(swiftCodeChildA, swiftCodeChildN));
        
        swiftCodesRepository.save(swiftCodeChildA);
        swiftCodesRepository.save(swiftCodeChildN);
        swiftCodesRepository.save(headSwiftCode);
    }
    
    
    String headSwiftCode = "BREXPLPWXXX";
    String branchSwiftCode = "BREXPLPWWAL";
    
    CountryItem countryPL = new CountryItem("PL", "POLAND");
    
    SwiftCodeItem swiftCodeData = SwiftCodeItem.builder()
        .swiftCode("ABCDEFGHXXX")
        .name("MAIN STREET BANK")
        .address("1234 Main St")
        .countryISO2(countryPL)
        .headquarter(true)
        .branches(new ArrayList<>())
        .build();
    
    SwiftCodeRequest swiftCodeRequest = SwiftCodeRequest.builder()
        .swiftCode(swiftCodeData.getSwiftCode())
        .name(swiftCodeData.getName())
        .address(swiftCodeData.getAddress())
        .countryISO2(countryPL.getCountryISO2())
        .countryName(countryPL.getCountryName())
        .headquarter(swiftCodeData.isHeadquarter())
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
            .body("countryISO2", is("PL"))
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
    public void whenGetNonExistentCode_thenNotFound() {
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
    public void whenPostSwiftCodeRequestAndIsHeadquarter_thenOkAndDataIsCorrect() throws JsonProcessingException {
        given()
            .contentType("application/json")
            .body(objectMapper.writeValueAsString(swiftCodeRequest))
            .when()
            .post("/")
            .then()
            .statusCode(201)
            .body(containsString("ok"));
        
        when()
            .get("/{swiftCode}", swiftCodeRequest.getSwiftCode())
            .then()
            .statusCode(200)
            .log().all()
            .body("swiftCode", is(swiftCodeRequest.getSwiftCode()))
            .body("name", is(swiftCodeRequest.getName()))
            .body("address", is(swiftCodeRequest.getAddress()))
            .body("countryISO2", is(swiftCodeRequest.getCountryISO2()))
            .body("countryName", is(swiftCodeRequest.getCountryName()))
            .body("isHeadquarter", is(true));
    }
    
    @Test
    public void whenPostSwiftCodeRequestAndHasHeadquarter_thenOk() throws JsonProcessingException {
        SwiftCodeRequest branchSwiftCodeRequest = swiftCodeRequest.toBuilder()
            .swiftCode(headSwiftCode.substring(0, 8) + "ABC")
            .headquarter(false)
            .build();
        
        given()
            .contentType("application/json")
            .body(objectMapper.writeValueAsString(branchSwiftCodeRequest))
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
            .body("branches.swiftCode", hasItem(branchSwiftCodeRequest.getSwiftCode()));
    }
    
    @Test
    public void whenPostSwiftCodeRequestAndHeadquarterNotExists_thenConflict() throws JsonProcessingException {
        SwiftCodeRequest branchSwiftCodeRequest = swiftCodeRequest.toBuilder()
            .swiftCode("NOTEXIST" + "ABC")
            .headquarter(false)
            .build();
        
        given()
            .contentType("application/json")
            .body(objectMapper.writeValueAsString(branchSwiftCodeRequest))
            .when()
            .post("/")
            .then()
            .statusCode(409)
            .body(containsString("Headquarter SWIFT code does not exists"));
    }
    
    @Test
    public void whenPostSwiftCodeRequestAndSwiftCodeDataExists_thenConflict() throws JsonProcessingException {
        SwiftCodeRequest branchSwiftCodeRequest = swiftCodeRequest.toBuilder()
            .swiftCode(branchSwiftCode)
            .headquarter(false)
            .build();
        
        given()
            .contentType("application/json")
            .body(objectMapper.writeValueAsString(branchSwiftCodeRequest))
            .when()
            .post("/")
            .then()
            .statusCode(409)
            .body(containsString("SWIFT code already exists"));
    }
    
    @Test
    public void whenGetSwiftCodesByCountryISO2_thenOk() {
        String countryISO2 = "PL";
        
        when()
            .get("/country/{countryISO2}", countryISO2)
            .then()
            .statusCode(200)
            .body("swiftCodes.swiftCode", hasItem(headSwiftCode))
            .body("swiftCodes.swiftCode", hasItem(branchSwiftCode));
    }
    
    @Test
    public void whenGetSwiftCodesByCountryISO2AndCountryDoesNotExists_thenNotFound() {
        String countryISO2 = "QQ";
        
        when()
            .get("/country/{countryISO2}", countryISO2)
            .then()
            .statusCode(404)
            .body(containsString("Country does not exists"));
    }
    
    @Test
    public void whenDeleteCode_thenOk() {
        when()
            .delete("/{swiftCode}", branchSwiftCode)
            .then()
            .statusCode(200);
        
        when()
            .get("/{swiftCode}", branchSwiftCode)
            .then()
            .statusCode(404);
    }
    
    @Test
    public void whenDeleteCodeAndSwiftCodeDataDoesNotExists_thenNotFound() {
        String swiftCode = "ABCDEFGHIJK";
        
        when()
            .delete("/{swiftCode}", swiftCode)
            .then()
            .statusCode(404);
    }
    
    @Test
    public void whenDeleteHeadCodeAndHeadCodeHasBranches_thenConflict() {
        when()
            .delete("/{swiftCode}", headSwiftCode)
            .then()
            .statusCode(409)
            .body(containsString("Child branches found"));
    }
}
