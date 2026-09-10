package com.example.erudio.integrationtests.controllers.emailTest;

import com.example.erudio.config.TestConfigs;
import com.example.erudio.integrationtests.dto.AccountCredentialsDTO;
import com.example.erudio.integrationtests.dto.EmailRequestDTO;
import com.example.erudio.integrationtests.dto.TokenDTO;
import com.example.erudio.integrationtests.testcontainers.AbstractIntegrationTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmailControllerXmlCorsOriginTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static TokenDTO tokenDTO;
    private static EmailRequestDTO emailRequestDTO;
    private static XmlMapper objectMapper;


    @BeforeAll
    static void setUp() {
        emailRequestDTO = new EmailRequestDTO();
        objectMapper = new XmlMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    }
    @Test
    @Order(0)
    void signIn() throws JsonProcessingException {
        AccountCredentialsDTO credentials =
                new AccountCredentialsDTO("leandro", "admin123");

        var content = given()
                .basePath("/auth/signin")
                .port(TestConfigs.SERVER_PORT)
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .accept(MediaType.APPLICATION_XML_VALUE)
                .body(credentials)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        tokenDTO = objectMapper.readValue(content, TokenDTO.class);

        assertNotNull(tokenDTO.getAccessToken());
        assertNotNull(tokenDTO.getRefreshToken());
    }

    @Test
    @Order(1)
    @Disabled("Desabilitado porque o envio de e-mail real está desativado")
    void sendEmailXmlOriginErudioTest() {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_ERUDIO)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/email/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        /// Fill in the fields according to your class.
        //emailRequestDTO.setTo("...");
        //emailRequestDTO.setSubject("...");
        //emailRequestDTO.setBody("...");

        var content = given(specification)
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .body(emailRequestDTO)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertNotNull(content);
        assertEquals("e-Mail sent with success!", content);
    }

    @Test
    @Order(2)
    @Disabled("Desabilitado porque o envio de e-mail real está desativado")
    void sendEmailXmlOriginLocalTest() {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/email/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        /// Fill in the fields according to your class.
        //emailRequestDTO.setTo("...");
        //emailRequestDTO.setSubject("...");
        //emailRequestDTO.setBody("...");


        var content = given(specification)
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .body(emailRequestDTO)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertNotNull(content);
        assertEquals("e-Mail sent with success!", content);
    }

    @Test
    @Order(3)
    @Disabled("Desabilitado porque o envio de e-mail real está desativado")
    void sendEmailXmlWithWrongOrigin() {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/email/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        /// Fill in the fields according to your class.
        //emailRequestDTO.setTo("...");
        //emailRequestDTO.setSubject("...");
        //emailRequestDTO.setBody("...");

        var content = given(specification)
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .body(emailRequestDTO)
                .when()
                .post()
                .then()
                .statusCode(403)
                .extract()
                .body()
                .asString();

        assertEquals("Invalid CORS request", content);
    }

}