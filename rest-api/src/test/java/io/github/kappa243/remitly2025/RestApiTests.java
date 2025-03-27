package io.github.kappa243.remitly2025;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

public class RestApiTests extends CommonTestModule {
    
    
    String headSwiftCode = "BREXPLPWXXX";
    String branchSwiftCode = "BREXPLPWWAL";
    
    //
    
    @Test
    public void whenGetHeadCode_thenOk() {
        when()
            .get("/{swiftCode}", headSwiftCode)
            .then()
            .statusCode(200);
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
            .log().all()
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
    
}
