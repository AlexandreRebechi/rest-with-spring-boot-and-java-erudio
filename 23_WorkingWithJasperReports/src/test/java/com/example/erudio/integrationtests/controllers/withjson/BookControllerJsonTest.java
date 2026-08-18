package com.example.erudio.integrationtests.controllers.withjson;

import com.example.erudio.config.TestConfigs;
import com.example.erudio.integrationtests.dto.BookDTO;
import com.example.erudio.integrationtests.dto.wrappers.json.WrapperBookDTO;
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

import java.util.Date;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookControllerJsonTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static ObjectMapper objectMapper;

    private static BookDTO book;


    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        book = new BookDTO();
    }

    @Test
    @Order(1)
    void createTest() throws JsonProcessingException {
        mockBook();
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(book)
                .when()
                .post()
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                .body()
                .asString();

        BookDTO createdBook = objectMapper.readValue(content, BookDTO.class);
        book = createdBook;

        assertNotNull(createdBook.getId());
        assertNotNull(book.getId());
        assertEquals("Docker Deep Dive - Updated", book.getTitle());
        assertEquals("Nigel Poulton", book.getAuthor());
        assertEquals(55.99, book.getPrice());
        assertTrue(createdBook.getEnabled());


    }

    @Test
    @Order(2)
    void updateTest() throws JsonProcessingException {
        book.setTitle("Docker Deep Dive - Updated");

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(book)
                .when()
                .put()
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                .body()
                .asString();

        BookDTO createdBook = objectMapper.readValue(content, BookDTO.class);
        book = createdBook;

        assertNotNull(createdBook.getId());
        assertTrue(createdBook.getId() > 0);

        assertNotNull(createdBook.getId());
        assertNotNull(book.getId());
        assertEquals("Docker Deep Dive - Updated", book.getTitle());
        assertEquals("Nigel Poulton", book.getAuthor());
        assertEquals(55.99, book.getPrice());
        assertTrue(createdBook.getEnabled());
    }

    @Test
    @Order(3)
    void findByIdTest() throws JsonProcessingException {

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", book.getId())
                .when()
                .get("{id}")
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                .body()
                .asString();

        BookDTO createdBook = objectMapper.readValue(content, BookDTO.class);
        book = createdBook;

        assertNotNull(createdBook.getId());
        assertTrue(createdBook.getId() > 0);

        assertNotNull(createdBook.getId());
        assertNotNull(book.getId());
        assertEquals("Docker Deep Dive - Updated", book.getTitle());
        assertEquals("Nigel Poulton", book.getAuthor());
        assertEquals(55.99, book.getPrice());
        assertTrue(createdBook.getEnabled());
    }

    @Test
    @Order(4)
    void disableTest() throws JsonProcessingException {

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", book.getId())
                .when()
                .patch("{id}")
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                .body()
                .asString();

        BookDTO createdBook = objectMapper.readValue(content, BookDTO.class);
        book = createdBook;

        assertNotNull(createdBook.getId());
        assertTrue(createdBook.getId() > 0);

        assertNotNull(createdBook.getId());
        assertNotNull(book.getId());
        assertEquals("Docker Deep Dive - Updated", book.getTitle());
        assertEquals("Nigel Poulton", book.getAuthor());
        assertEquals(55.99, book.getPrice());
        assertFalse(createdBook.getEnabled());
    }

    @Test
    @Order(5)
    void deleteTest() throws JsonProcessingException {
        given(specification)
                .pathParam("id", book.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(6)
    void findAllTest() throws JsonProcessingException {

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

        WrapperBookDTO wrapper = objectMapper.readValue(content, WrapperBookDTO.class);
        var books = wrapper.getEmbedded().getBooks();

        BookDTO bookOne = books.get(0);

        assertNotNull(bookOne.getId());
        assertNotNull(bookOne.getTitle());
        assertNotNull(bookOne.getAuthor());
        assertNotNull(bookOne.getPrice());
        assertTrue(bookOne.getId() > 0);
        assertEquals("Ameliorated fresh-thinking encryption", bookOne.getTitle());
        assertEquals("Robinett Samter", bookOne.getAuthor());
        assertEquals(18.99, bookOne.getPrice());
        assertTrue(bookOne.getEnabled());

        BookDTO bookFour = books.get(4);

        assertNotNull(bookFour.getId());
        assertNotNull(bookFour.getTitle());
        assertNotNull(bookFour.getAuthor());
        assertNotNull(bookFour.getPrice());
        assertTrue(bookFour.getId() > 0);
        assertEquals("Ameliorated radical analyzer", bookFour.getTitle());
        assertEquals("Anatol Liebrecht", bookFour.getAuthor());
        assertEquals(24.99, bookFour.getPrice());
        assertTrue(bookOne.getEnabled());
    }

    @Test
    @Order(14)
    void findByAuthorTest() throws JsonProcessingException {
        //{{baseUrl}}/api/book/v1/findBooksByAuthor/and?page=0&size=2&direction=asc
        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("author", "and")
                .queryParams("page", 0, "size", 12, "direction", "asc")
                .when()
                .get("findBooksByAuthor/{author}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        WrapperBookDTO wrapper = objectMapper.readValue(content, WrapperBookDTO.class);
        var books = wrapper.getEmbedded().getBooks();

        for (BookDTO book : books) {
            System.out.println(
                    "ID: " + book.getId()
                            + " | AUTHOR: " + book.getAuthor()
                            + " | TITLE: " + book.getTitle()
                            + " | PRICE: " + book.getPrice()
            );

            BookDTO bookOne = books.get(0);

            assertNotNull(bookOne.getId());
            assertNotNull(bookOne.getTitle());
            assertNotNull(bookOne.getAuthor());
            assertNotNull(bookOne.getPrice());
            assertTrue(bookOne.getId() > 0);
            assertEquals("Aguinaldo Aragon Fernandes e Vladimir Ferraz de Abreu", bookOne.getAuthor());
            assertEquals("Implantando a governança de TI", bookOne.getTitle());
            assertEquals(54.0, bookOne.getPrice());
            assertTrue(bookOne.getEnabled());

            BookDTO bookFour = books.get(4);

            assertNotNull(bookFour.getId());
            assertNotNull(bookFour.getTitle());
            assertNotNull(bookFour.getAuthor());
            assertNotNull(bookFour.getPrice());
            assertTrue(bookFour.getId() > 0);
            assertEquals("Synergistic responsive installation", bookFour.getTitle());
            assertEquals("Alejandro Queree", bookFour.getAuthor());
            assertEquals(89.99, bookFour.getPrice());
            assertTrue(bookOne.getEnabled());
        }


    }

    private void mockBook() {
        book.setTitle("Docker Deep Dive - Updated");
        book.setAuthor("Nigel Poulton");
        book.setPrice(Double.valueOf(55.99));
        book.setLaunchDate(new Date());
        book.setEnabled(true);
    }
}