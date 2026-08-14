package com.example.erudio.integrationtests.controllers.withxml;

import com.example.erudio.config.TestConfigs;
import com.example.erudio.integrationtests.dto.BookDTO;
import com.example.erudio.integrationtests.dto.PersonDTO;
import com.example.erudio.integrationtests.dto.wrappers.json.WrapperBookDTO;
import com.example.erudio.integrationtests.dto.wrappers.xmlandyaml.PagedModelBook;
import com.example.erudio.integrationtests.testcontainers.AbstractIntegrationTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
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
class BookControllerXmlTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static XmlMapper xmlMapper;

    private static BookDTO book;


    @BeforeAll
    static void setUp() {

        xmlMapper = new XmlMapper();
        xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        book = new BookDTO();
    }

    @Test
    @Order(1)
    void createTest() throws JsonProcessingException {
        mockbook();
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .setContentType(ContentType.XML)
                .build();

        var content = given(specification)
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .accept(MediaType.APPLICATION_XML_VALUE)
                    .body(book)
                .when()
                    .post()
                .then()
                    .statusCode(200)
                //.contentType(MediaType.APPLICATION_XML_VALUE)
                .extract()
                    .body()
                        .asString();

        BookDTO createdBook = xmlMapper.readValue(content, BookDTO.class);
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
    @Order(2)
    void updateTest() throws JsonProcessingException {
        book.setTitle("Docker Deep Dive - Updated");

        var content = given(specification)
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .accept(MediaType.APPLICATION_XML_VALUE)
                    .body(book)
                .when()
                //.contentType(MediaType.APPLICATION_XML_VALUE)
                    .put()
                .then()
                    .statusCode(200)
                //.contentType(MediaType.APPLICATION_XML_VALUE)
                .extract()
                    .body()
                        .asString();

        BookDTO createdBook = xmlMapper.readValue(content, BookDTO.class);
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
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .accept(MediaType.APPLICATION_XML_VALUE)
                .pathParam("id", book.getId())
                .when()
                .get("{id}")
                .then()
                .statusCode(200)
                //.contentType(MediaType.APPLICATION_XML_VALUE)
                .extract()
                .body()
                .asString();

        BookDTO createdBook = xmlMapper.readValue(content, BookDTO.class);
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
    void disableBookTest() throws JsonProcessingException {

        var content = given(specification)
                    .accept(MediaType.APPLICATION_XML_VALUE)
                    .pathParam("id", book.getId())
                .when()
                    .patch("{id}")
                .then()
                    .statusCode(200)
               // .contentType(MediaType.APPLICATION_XML_VALUE)
                .extract()
                    .body()
                        .asString();

        BookDTO createdBook = xmlMapper.readValue(content, BookDTO.class);
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
                .accept(MediaType.APPLICATION_XML_VALUE)
                .queryParams("page", 3, "size", 12, "direction", "asc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .extract()
                .body()
                .asString();

        PagedModelBook wrapper = xmlMapper.readValue(content, PagedModelBook.class);
        List<BookDTO> books = wrapper.getContent();

        BookDTO bookOne = books.get(0);

        assertNotNull(bookOne.getId());
        assertNotNull(bookOne.getTitle());
        assertNotNull(bookOne.getAuthor());
        assertNotNull(bookOne.getPrice());
        assertTrue(bookOne.getId() > 0);
        assertEquals("Assimilated real-time function", bookOne.getTitle());
        assertEquals("Leonora Hardwell", bookOne.getAuthor());
        assertEquals(9.99, bookOne.getPrice());
        assertTrue(bookOne.getEnabled());

        BookDTO bookFour = books.get(4);

        assertNotNull(bookFour.getId());
        assertNotNull(bookFour.getTitle());
        assertNotNull(bookFour.getAuthor());
        assertNotNull(bookFour.getPrice());
        assertTrue(bookFour.getId() > 0);
        assertEquals("Automated local function", bookFour.getTitle());
        assertEquals("Sascha Scardafield", bookFour.getAuthor());
        assertEquals(3.99, bookFour.getPrice());
        assertTrue(bookOne.getEnabled());
    }

    @Test
    @Order(7)
    void findByAuthorTest() throws JsonProcessingException {
        //{{baseUrl}}/api/book/v1/findBooksByAuthor/and?page=0&size=2&direction=asc
        var content = given(specification)
                .accept(MediaType.APPLICATION_XML_VALUE)
                .pathParam("author","and")
                .queryParams("page", 0, "size", 12, "direction", "asc")
                .when()
                .get("findBooksByAuthor/{author}")
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .extract()
                .body()
                .asString();

        PagedModelBook wrapper = xmlMapper.readValue(content, PagedModelBook.class);
        List<BookDTO> books = wrapper.getContent();


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

   private void mockbook() {
       book.setTitle("Docker Deep Dive - Updated");
       book.setAuthor("Nigel Poulton");
       book.setPrice(Double.valueOf(55.99));
       book.setLaunchDate(new Date());
       book.setEnabled(true);
   }
}