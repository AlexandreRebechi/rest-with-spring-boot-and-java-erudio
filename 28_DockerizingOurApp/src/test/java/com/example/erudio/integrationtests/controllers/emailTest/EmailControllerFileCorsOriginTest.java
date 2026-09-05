package com.example.erudio.integrationtests.controllers.emailTest;

import com.example.erudio.config.TestConfigs;
import com.example.erudio.integrationtests.dto.AccountCredentialsDTO;
import com.example.erudio.integrationtests.dto.TokenDTO;
import com.example.erudio.integrationtests.testcontainers.AbstractIntegrationTest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmailControllerFileCorsOriginTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static TokenDTO tokenDTO;



    @BeforeAll
    static void setUp() {
        tokenDTO = new TokenDTO();
    }
    @Test
    @Order(0)
    void signIn() {
        AccountCredentialsDTO credentials =
                new AccountCredentialsDTO("leandro", "admin123");

        tokenDTO = given()
                .basePath("/auth/signin")
                .port(TestConfigs.SERVER_PORT)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(credentials)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(TokenDTO.class);



        assertNotNull(tokenDTO.getAccessToken());
        assertNotNull(tokenDTO.getRefreshToken());
    }


    @Test
    @Order(1)
    @Disabled("Teste depende de servidor SMTP externo")
    void sendEmailWithAttachmentOriginErudioTest() {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_ERUDIO)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/email/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        String emailRequest = """
                {
                    "to": "teste@example.com",
                    "subject": "Teste",
                    "body": "Mensagem de teste"
                }
                """;

        byte[] file = "Arquivo de teste"
                .getBytes(StandardCharsets.UTF_8);

        var content = given(specification)
                .multiPart(
                        "emailRequest",
                        emailRequest
                )
                .multiPart(
                        "attachment",
                        "test.txt",
                        file,
                        "text/plain"
                )
                .when()
                .post("/withAttachment")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertNotNull(content);

        assertEquals("e-Mail with attachment sent successfully!", content);
    }

    @Test
    @Order(2)
    @Disabled("Teste depende de servidor SMTP externo")
    void sendEmailWithAttachmentOriginLocalTest() {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/email/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        String emailRequest = """
                {
                    "to": "teste@example.com",
                    "subject": "Teste",
                    "body": "Mensagem de teste"
                }
                """;

        byte[] file = "Arquivo de teste"
                .getBytes(StandardCharsets.UTF_8);

        var content = given(specification)
                .multiPart(
                        "emailRequest",
                        emailRequest
                )
                .multiPart(
                        "attachment",
                        "test.txt",
                        file,
                        "text/plain"
                )
                .when()
                .post("/withAttachment")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertNotNull(content);

        assertEquals("e-Mail with attachment sent successfully!", content);
    }
    @Test
    @Order(3)
    void sendEmailWithAttachmentWithWrongOriginTest() {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/email/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        String emailRequest = """
                {
                    "to": "teste@example.com",
                    "subject": "Teste",
                    "body": "Mensagem de teste"
                }
                """;

        byte[] file = "Arquivo de teste"
                .getBytes(StandardCharsets.UTF_8);

        var content = given(specification)
                .multiPart(
                        "emailRequest",
                        emailRequest
                )
                .multiPart(
                        "attachment",
                        "test.txt",
                        file,
                        "text/plain"
                )
                .when()
                .post("/withAttachment")
                .then()
                .statusCode(403)
                .extract()
                .body()
                .asString();

        assertEquals("Invalid CORS request", content);
    }
}