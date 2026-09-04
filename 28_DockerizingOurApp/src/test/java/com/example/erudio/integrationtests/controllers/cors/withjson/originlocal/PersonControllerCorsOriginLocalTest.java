package com.example.erudio.integrationtests.controllers.cors.withjson.originlocal;

import com.example.erudio.config.TestConfigs;
import com.example.erudio.file.exporter.MediaTypes;
import com.example.erudio.integrationtests.dto.AccountCredentialsDTO;
import com.example.erudio.integrationtests.dto.PersonDTO;
import com.example.erudio.integrationtests.dto.TokenDTO;
import com.example.erudio.integrationtests.testcontainers.AbstractIntegrationTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PersonControllerCorsOriginLocalTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static ObjectMapper objectMapper;

    private static PersonDTO person;
    private static TokenDTO tokenDTO;


    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        person = new PersonDTO();
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
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
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
    void create() throws JsonProcessingException {
        mockPerson();
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(person)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        PersonDTO createdPerson = objectMapper.readValue(content, PersonDTO.class);
        person = createdPerson;

        assertNotNull(createdPerson.getId());
        assertNotNull(createdPerson.getFirstName());
        assertNotNull(createdPerson.getLastName());
        assertNotNull(createdPerson.getAddress());
        assertNotNull(createdPerson.getGender());
        assertNotNull(createdPerson.getProfileUrl());
        assertNotNull(createdPerson.getPhotoUrl());


        assertTrue(createdPerson.getId() > 0);

        assertEquals("Richard", createdPerson.getFirstName());
        assertEquals("Stallman", createdPerson.getLastName());
        assertEquals("New York City - New York - USA", createdPerson.getAddress());
        assertEquals("Male", createdPerson.getGender());
        assertTrue(createdPerson.getEnabled());

    }

    @Test
    @Order(2)
    void createWithWrongOrigin() throws JsonProcessingException {
        mockPerson();
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(person)
                .when()
                .post()
                .then()
                .statusCode(403)
                .extract()
                .body()
                .asString();

    }



    @Test
    @Order(3)
    void update() throws JsonProcessingException {
        person.setLastName("Stallman");
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(person)
                .when()
                .put()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        PersonDTO createdPerson = objectMapper.readValue(content, PersonDTO.class);
        person = createdPerson;

        assertNotNull(createdPerson.getId());
        assertNotNull(createdPerson.getFirstName());
        assertNotNull(createdPerson.getLastName());
        assertNotNull(createdPerson.getAddress());
        assertNotNull(createdPerson.getGender());
        assertNotNull(createdPerson.getProfileUrl());
        assertNotNull(createdPerson.getPhotoUrl());

        assertTrue(createdPerson.getId() > 0);

        assertEquals("Richard", createdPerson.getFirstName());
        assertEquals("Stallman", createdPerson.getLastName());
        assertEquals("New York City - New York - USA", createdPerson.getAddress());
        assertEquals("Male", createdPerson.getGender());
        assertTrue(createdPerson.getEnabled());
    }

    @Test
    @Order(4)
    void updateWithWrongOrigin() throws JsonProcessingException {
        mockPerson();
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(person)
                .when()
                .put()
                .then()
                .statusCode(403)
                .extract()
                .body()
                .asString();

        assertEquals("Invalid CORS request", content);


    }

    @Test
    @Order(5)
    void findById() throws JsonProcessingException {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .pathParam("id", person.getId())
                .when()
                    .get("{id}")
                .then()
                    .statusCode(200)
                .extract()
                    .body()
                        .asString();

        PersonDTO createdPerson = objectMapper.readValue(content, PersonDTO.class);
        person = createdPerson;

        assertNotNull(createdPerson.getId());
        assertNotNull(createdPerson.getFirstName());
        assertNotNull(createdPerson.getLastName());
        assertNotNull(createdPerson.getAddress());
        assertNotNull(createdPerson.getGender());
        assertNotNull(createdPerson.getProfileUrl());
        assertNotNull(createdPerson.getPhotoUrl());

        assertTrue(createdPerson.getId() > 0);

        assertEquals("Richard", createdPerson.getFirstName());
        assertEquals("Stallman", createdPerson.getLastName());
        assertEquals("New York City - New York - USA", createdPerson.getAddress());
        assertEquals("Male", createdPerson.getGender());
        assertTrue(createdPerson.getEnabled());
    }

    @Test
    @Order(6)
    void findByIdWithWrongOrigin() throws JsonProcessingException {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", person.getId())
                    .when()
                .get("{id}")
                    .then()
                .statusCode(403)
                    .extract()
                        .body()
                            .asString();

        assertEquals("Invalid CORS request", content);
    }

    @Test
    @Order(7)
    void disable() throws JsonProcessingException {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", person.getId())
                .when()
                .patch("{id}")
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                .body()
                .asString();

        PersonDTO createdPerson = objectMapper.readValue(content, PersonDTO.class);
        person = createdPerson;

        assertNotNull(createdPerson.getId());
        assertTrue(createdPerson.getId() > 0);

        assertEquals("Richard", createdPerson.getFirstName());
        assertEquals("Stallman", createdPerson.getLastName());
        assertEquals("New York City - New York - USA", createdPerson.getAddress());
        assertEquals("Male", createdPerson.getGender());
        assertFalse(createdPerson.getEnabled());
    }

    @Test
    @Order(8)
    void disableWithWrongOrigin() throws JsonProcessingException {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", person.getId())
                .when()
                .patch("{id}")
                .then()
                .statusCode(403)
                .extract()
                .body()
                .asString();

        assertEquals("Invalid CORS request", content);
    }

    @Test
    @Order(9)
    void delete() throws JsonProcessingException {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        given(specification)
                .pathParam("id", person.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);


    }

    @Test
    @Order(10)
    void deleteWithWrongOrigin() throws JsonProcessingException {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        given(specification)
                .pathParam("id", person.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(403);


    }

    @Test
    @Order(11)
    void findAll() throws JsonProcessingException {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParams("page", 3, "size", 12, "direction", "asc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

// CORREÇÃO CRÍTICA: Lê diretamente a árvore do JSON e extrai a lista do nó "content"
        com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(content);
        com.fasterxml.jackson.databind.JsonNode contentNode = rootNode.path("content");

// Converte o nó "content" diretamente para a lista de PersonDTO compatível com Spring Boot 4
        List<PersonDTO> people = objectMapper.readValue(
                contentNode.toString(),
                new com.fasterxml.jackson.core.type.TypeReference<List<PersonDTO>>(){}
        );
        PersonDTO personOne = people.get(0);

        assertNotNull(personOne.getId());
        assertTrue(personOne.getId() > 0);

        assertEquals("Aloise", personOne.getFirstName());
        assertEquals("Galletley", personOne.getLastName());
        assertEquals("Apt 512", personOne.getAddress());
        assertEquals("Female", personOne.getGender());
        assertFalse(personOne.getEnabled());

        PersonDTO personFour = people.get(4);

        assertNotNull(personFour.getId());
        assertTrue(personFour.getId() > 0);

        assertEquals("Amata", personFour.getFirstName());
        assertEquals("Karslake", personFour.getLastName());
        assertEquals("Suite 97", personFour.getAddress());
        assertEquals("Female", personFour.getGender());
        assertFalse(personFour.getEnabled());
    }
    @Test
    @Order(12)
    void findAllWithWrongOrigin() {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get()
                .then()
                .statusCode(403)
                .extract()
                .body()
                .asString();

        assertEquals("Invalid CORS request", content);
    }

    @Test
    @Order(13)
    void findByName() throws JsonProcessingException {
        //{{baseUrl}}/api/person/v1/findPeopleByName/and?page=0&size=2&direction=asc
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("firstName","and")
                .queryParams("page", 0, "size", 12, "direction", "asc")
                .when()
                .get("findPeopleByName/{firstName}")
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                .body()
                .asString();

        // Lê diretamente a árvore do JSON e extrai a lista do nó "content"
        com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(content);
        com.fasterxml.jackson.databind.JsonNode contentNode = rootNode.path("content");

        // Converte o nó "content" diretamente para a lista de PersonDTO compatível com Spring Boot 4
        List<PersonDTO> people = objectMapper.readValue(
                contentNode.toString(),
                new com.fasterxml.jackson.core.type.TypeReference<List<PersonDTO>>(){}
        );
        PersonDTO personOne = people.get(0);


        assertNotNull(personOne.getId());
        assertTrue(personOne.getId() > 0);

        assertEquals("Alejandro", personOne.getFirstName());
        assertEquals("Weymouth", personOne.getLastName());
        assertEquals("Room 1522", personOne.getAddress());
        assertEquals("Male", personOne.getGender());
        //assertTrue(personOne.getEnabled());
        assertFalse(personOne.getEnabled());

        PersonDTO personFour = people.get(4);

        assertNotNull(personFour.getId());
        assertTrue(personFour.getId() > 0);

        assertEquals("Andy", personFour.getFirstName());
        assertEquals("Salvage", personFour.getLastName());
        assertEquals("10th Floor", personFour.getAddress());
        assertEquals("Female", personFour.getGender());
        //assertTrue(personFour.getEnabled());
        assertFalse(personFour.getEnabled());
    }
    @Test
    @Order(14)
    void findByNameWithWrongOrigin() throws JsonProcessingException {
        //{{baseUrl}}/api/person/v1/findPeopleByName/and?page=0&size=2&direction=asc
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("firstName","and")
                .queryParams("page", 0, "size", 12, "direction", "asc")
                .when()
                .get("findPeopleByName/{firstName}")
                .then()
                .statusCode(403)
                .extract()
                .body()
                .asString();

        assertEquals("Invalid CORS request", content);

    }
    @Test
    @Order(15)
    void massCreationCsv() throws Exception {

        mockPerson();

        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1/massCreation")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        byte[] file = """
            first_name;last_name;address;gender;enabled
            Richard;Stallman;New York City - New York - USA;Male;true
            João;Silva;Rua B;Female;true
            """.getBytes(StandardCharsets.UTF_8);

        var content = given(specification)
                .multiPart("file", "mockPerson.csv", file, MediaTypes.APPLICATION_CSV_VALUE)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        List<PersonDTO> people = objectMapper.readValue(
                content,
                new com.fasterxml.jackson.core.type.TypeReference<List<PersonDTO>>(){}
        );

        assertNotNull(people);
        assertEquals(2, people.size());

        assertNotNull(people.get(0).getId());
        assertEquals("Richard", people.get(0).getFirstName());
        assertEquals("Stallman", people.get(0).getLastName());
        assertEquals("New York City - New York - USA", people.get(0).getAddress());
        assertEquals("Male", people.get(0).getGender());
        assertTrue(people.get(0).getEnabled());

        assertNotNull(people.get(1).getId());
        assertEquals("João", people.get(1).getFirstName());
        assertEquals("Silva", people.get(1).getLastName());
        assertEquals("Rua B", people.get(1).getAddress());
        assertEquals("Female", people.get(1).getGender());
        assertTrue(people.get(1).getEnabled());
    }
    @Test
    @Order(16)
    void massCreationCsvWithWrongOrigin() throws Exception {
        mockPerson();
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1/massCreation")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        byte[] file = """
            first_name;last_name;address;gender
            Richard;Stallman;New York City - New York - USA;Male
            João;Silva;Rua B;Female
            """.getBytes(StandardCharsets.UTF_8);
        var content = given(specification).multiPart("file", "mockPerson.csv", file, MediaTypes.APPLICATION_CSV_VALUE)
                .when()
                .post()
                .then()
                .statusCode(403)
                .extract()
                .body()
                .asString();

        assertEquals("Invalid CORS request", content);
    }

    @Test
    @Order(16)
    void massCreationXlsx() throws Exception {

        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/person/v1/massCreation")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("MockPerson");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("First Name");
        header.createCell(1).setCellValue("Last Name");
        header.createCell(2).setCellValue("Address");
        header.createCell(3).setCellValue("gender");
        header.createCell(4).setCellValue("Enabled");
        header.createCell(5).setCellValue("Profile Url");
        header.createCell(6).setCellValue("Photo Url");

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("Richard");
        row1.createCell(1).setCellValue("Stallman");
        row1.createCell(2).setCellValue("New York City - New York - USA");
        row1.createCell(3).setCellValue("Male");
        row1.createCell(4).setCellValue("https://pt.wikipedia.org/wiki/Richard_Stallman");
        row1.createCell(5).setCellValue("https://pt.wikipedia.org/wiki/Ficheiro:Richard_Stallman_-_F%C3%AAte_de_l'Humanit%C3%A9_2014_-_010.jpg");

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("Richard");
        row2.createCell(1).setCellValue("Stallman");
        row2.createCell(2).setCellValue("New York City - New York - USA");
        row2.createCell(3).setCellValue("Male");
        row2.createCell(4).setCellValue("https://pt.wikipedia.org/wiki/Richard_Stallman");
        row2.createCell(5).setCellValue("https://pt.wikipedia.org/wiki/Ficheiro:Richard_Stallman_-_F%C3%AAte_de_l'Humanit%C3%A9_2014_-_010.jpg");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        var content = given(specification)
                .multiPart("file", "mockPerson.xlsx", outputStream.toByteArray(), MediaTypes.APPLICATION_XLSX_VALUE)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        List<PersonDTO> mock = objectMapper.readValue(
                content,
                new com.fasterxml.jackson.core.type.TypeReference<List<PersonDTO>>(){}

        );

        assertNotNull(mock);
        assertEquals(2, mock.size());

        assertEquals("Richard", mock.get(0).getFirstName());
        assertEquals("Stallman",mock.get(0).getLastName());
        assertEquals("New York City - New York - USA", mock.get(0).getAddress());
        assertEquals("Male", mock.get(0).getGender());
        assertTrue(mock.get(0).getEnabled());


        assertEquals("Richard", mock.get(1).getFirstName());
        assertEquals("Stallman",mock.get(1).getLastName());
        assertEquals("New York City - New York - USA", mock.get(1).getAddress());
        assertEquals("Male", mock.get(1).getGender());
        assertTrue(mock.get(1).getEnabled());

    }
    @Test
    @Order(17)
    void massCreationXlsxWithWrongOrigin() throws Exception {

        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/book/v1/massCreation")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();



        var content = given(specification)
                .multiPart("file", "mockPerson.xlsx", MediaTypes.APPLICATION_XLSX_VALUE)
                .when()
                .post()
                .then()
                .statusCode(403)
                .extract()
                .body()
                .asString();

        assertEquals("Invalid CORS request", content);


    }

    @Test
    @Order(18)
    void exportPageCsv() throws JsonProcessingException{
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .addHeader("Accept", MediaTypes.APPLICATION_CSV_VALUE)
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .queryParams("page", 3, "size", 12, "direction", "asc")
                .when()
                .get("/exportPage")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertNotNull(content.contains("id"));
        assertNotNull(content.contains("first_name"));
        assertNotNull(content.contains("last_name"));
        assertNotNull(content.contains("address"));
        assertNotNull(content.contains("gender"));


    }

    @Test
    @Order(18)
    void exportPageCsvWithWrongOrigin() throws JsonProcessingException{
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .addHeader("Accept", MediaTypes.APPLICATION_CSV_VALUE)
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .queryParams("page", 3, "size", 12, "direction", "asc")
                .when()
                .get("/exportPage")
                .then()
                .statusCode(403)
                .extract()
                .body()
                .asString();

    }

    @Test
    @Order(19)
    void exportPageXlsx() throws JsonProcessingException{
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .addHeader("Accept", MediaTypes.APPLICATION_XLSX_VALUE)
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .queryParams("page", 3, "size", 12, "direction", "asc")
                .when()
                .get("/exportPage")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertNotNull(content.contains("id"));
        assertNotNull(content.contains("first_name"));
        assertNotNull(content.contains("last_name"));
        assertNotNull(content.contains("address"));
        assertNotNull(content.contains("gender"));

    }

    @Test
    @Order(20)
    void exportPageXlsxWithWrongOrigin() throws JsonProcessingException{
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .addHeader("Accept", MediaTypes.APPLICATION_XLSX_VALUE)
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .queryParams("page", 3, "size", 12, "direction", "asc")
                .when()
                .get("/exportPage")
                .then()
                .statusCode(403)
                .extract()
                .body()
                .asString();

        assertEquals("Invalid CORS request", content);

    }

    @Test
    @Order(21)
    void exportPagePdf() throws JsonProcessingException{
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .addHeader("Accept", MediaTypes.APPLICATION_PDF_VALUE)
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .queryParams("page", 3, "size", 12, "direction", "asc")
                .when()
                .get("/exportPage")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertNotNull(content.contains("id"));
        assertNotNull(content.contains("first_name"));
        assertNotNull(content.contains("last_name"));
        assertNotNull(content.contains("address"));
        assertNotNull(content.contains("gender"));

    }

    @Test
    @Order(22)
    void exportPdfWithWrongOrigin() {

        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .addHeader("Accept", MediaTypes.APPLICATION_PDF_VALUE)
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        var response = given(specification)
                .queryParam("id", 1)
                .when()
                .get("/export/1")
                .then()
                .statusCode(403)
                .extract()
                .response();

    }

    @Test
    @Order(23)
    void exportPdf() {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .addHeader("Accept", MediaTypes.APPLICATION_PDF_VALUE)
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        var response = given(specification)
                .queryParam("id", 1)
                .when()
                .get("/export/1")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .response();

        assertNotNull(response);
        assertEquals(MediaTypes.APPLICATION_PDF_VALUE, response.getContentType().split(";")[0]
        );

        assertNotNull(response.getBody());
        assertTrue(response.getBody().asByteArray().length > 0);

        assertTrue(response.getHeader("Content-Disposition").contains("person.pdf"));
    }

    @Test
    @Order(24)
    void exportPagePdfWithWrongOrigin() throws JsonProcessingException{
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .addHeader("Accept", MediaTypes.APPLICATION_PDF_VALUE)
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)

                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParams("page", 3, "size", 12, "direction", "asc")
                .when()
                .get("/exportPage")
                .then()
                .statusCode(403)
                .extract()
                .body()
                .asString();

        assertEquals("Invalid CORS request", content);
    }



   private void mockPerson() {
       person.setFirstName("Richard");
       person.setLastName("Stallman");
       person.setAddress("New York City - New York - USA");
       person.setGender("Male");
       person.setEnabled(true);
       person.setProfileUrl("https://github.com/AlexandreRebechi/rest-with-spring-boot-and-java-erudio/tree/main/photos");
       person.setPhotoUrl("https://github.com/AlexandreRebechi/rest-with-spring-boot-and-java-erudio/blob/main/photos/00_some_person.jpg");
   }
}