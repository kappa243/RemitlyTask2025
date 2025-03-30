package io.github.kappa243.remitly2025;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseTestModule {
    
    @LocalServerPort
    int port;
    
    String URI = "http://localhost";
    String PATH = "/v1/swift-codes";
    
    @Container
    public static final GenericContainer mongoDBContainer = new GenericContainer(DockerImageName.parse("mongodb/mongodb-community-server:7.0-ubuntu2204"))
        .withEnv("MONGO_INITDB_ROOT_USERNAME", "user")
        .withEnv("MONGO_INITDB_ROOT_PASSWORD", "password")
        .withEnv("MONGO_INITDB_DATABASE", "swift")
        .withExposedPorts(27017);
    
    static {
        mongoDBContainer.start();
    }
    
    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> String.format(
            "mongodb://%s:%s@%s:%d/%s?authSource=admin",
            "user",
            "password",
            mongoDBContainer.getHost(),
            mongoDBContainer.getFirstMappedPort(),
            "swift"
        ));
    }
    
    @BeforeEach
    public void setupRestAssured() {
        RestAssured.baseURI = URI;
        RestAssured.basePath = PATH;
        RestAssured.port = port;
    }
    
    
}
