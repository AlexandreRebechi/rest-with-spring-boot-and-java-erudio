package com.example.erudio.integrationtests.controllers.withjson;

import com.example.erudio.config.TestConfigs;
import com.example.erudio.integrationtests.dto.AccountCredentialsDTO;
import com.example.erudio.integrationtests.dto.PersonDTO;
import com.example.erudio.integrationtests.dto.TokenDTO;
import com.example.erudio.integrationtests.dto.wrappers.json.WrapperPersonDTO;
import com.example.erudio.integrationtests.testcontainers.AbstractIntegrationTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PersonControllerJsonTest extends AbstractIntegrationTest {

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
    void createTest() throws JsonProcessingException {
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
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                    .body()
                        .asString();

        PersonDTO createdPerson = objectMapper.readValue(content, PersonDTO.class);
        person = createdPerson;

        assertNotNull(createdPerson.getId());
        assertTrue(createdPerson.getId() > 0);

        assertEquals("Linus", createdPerson.getFirstName());
        assertEquals("Torvalds", createdPerson.getLastName());
        assertEquals("Helsinki - Finland", createdPerson.getAddress());
        assertEquals("Male", createdPerson.getGender());
        assertTrue(createdPerson.getEnabled());


    }

    @Test
    @Order(2)
    void updateTest() throws JsonProcessingException {
        person.setLastName("Benedict Torvalds");

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(person)
                .when()
                    .put()
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

        assertEquals("Linus", createdPerson.getFirstName());
        assertEquals("Benedict Torvalds", createdPerson.getLastName());
        assertEquals("Helsinki - Finland", createdPerson.getAddress());
        assertEquals("Male", createdPerson.getGender());
        assertTrue(createdPerson.getEnabled());
    }

    @Test
    @Order(3)
    void findByIdTest() throws JsonProcessingException {

        var content = given(specification)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .pathParam("id", person.getId())
                .when()
                    .get("{id}")
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

        assertEquals("Linus", createdPerson.getFirstName());
        assertEquals("Benedict Torvalds", createdPerson.getLastName());
        assertEquals("Helsinki - Finland", createdPerson.getAddress());
        assertEquals("Male", createdPerson.getGender());
        assertTrue(createdPerson.getEnabled());
    }

    @Test
    @Order(4)
    void disableTest() throws JsonProcessingException {

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

        assertEquals("Linus", createdPerson.getFirstName());
        assertEquals("Benedict Torvalds", createdPerson.getLastName());
        assertEquals("Helsinki - Finland", createdPerson.getAddress());
        assertEquals("Male", createdPerson.getGender());
        assertFalse(createdPerson.getEnabled());
    }

    @Test
    @Order(5)
    void deleteTest() throws JsonProcessingException {
            given(specification)
                    .pathParam("id", person.getId())
                .when()
                    .delete("{id}")
                .then()
                    .statusCode(204);
    }

    @Test
    @Order(6)
    void findAllTest() throws JsonProcessingException {

        var content = given(specification)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .queryParams("page", 3, "size", 12, "direction", "asc")
                .when()
                .get()
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

        assertEquals("Aloise", personOne.getFirstName());
        assertEquals("Galletley", personOne.getLastName());
        assertEquals("Apt 512", personOne.getAddress());
        assertEquals("Female", personOne.getGender());
        //assertTrue(personOne.getEnabled());
        assertFalse(personOne.getEnabled());

        PersonDTO personFour = people.get(4);

        assertNotNull(personFour.getId());
        assertTrue(personFour.getId() > 0);

        assertEquals("Amata", personFour.getFirstName());
        assertEquals("Karslake", personFour.getLastName());
        assertEquals("Suite 97", personFour.getAddress());
        assertEquals("Female", personFour.getGender());
        //assertTrue(personFour.getEnabled());
        assertFalse(personFour.getEnabled());
    }

    @Test
    @Order(7)
    void findByNameTest() throws JsonProcessingException {
        //{{baseUrl}}/api/person/v1/findPeopleByName/and?page=0&size=2&direction=asc
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

   private void mockPerson() {
       person.setFirstName("Linus");
       person.setLastName("Torvalds");
       person.setAddress("Helsinki - Finland");
       person.setGender("Male");
       person.setEnabled(true);
   }
}