package com.example.erudio.integrationtests.controllers.cors.withjson.originerudio;

import com.example.erudio.config.TestConfigs;
import com.example.erudio.file.exporter.MediaTypes;
import com.example.erudio.integrationtests.dto.AccountCredentialsDTO;
import com.example.erudio.integrationtests.dto.BookDTO;
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
import java.util.Date;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookControllerCorsOriginErudioTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static ObjectMapper objectMapper;

    private static BookDTO book;
    private static TokenDTO tokenDTO;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        book = new BookDTO();
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
        mockBook();

        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
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
    void createWithWrongOrigin() throws JsonProcessingException {
        mockBook();

        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
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
                .statusCode(403)
                .extract()
                .body()
                .asString();

    }



    @Test
    @Order(3)
    void update() throws JsonProcessingException {
        book.setTitle("Docker Deep Dive - Updated");

        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(book)
                .when()
                .put()
                .then()
                .statusCode(200)
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
    void updateWithWrongOrigin() {
        mockBook();

        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(book)
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
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", book.getId())
                .when()
                .get("{id}")
                .then()
                .statusCode(200)
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
    @Order(6)
    void findByIdWithWrongOrigin() {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", book.getId())
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
    void disableBook() throws JsonProcessingException {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

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
    @Order(8)
    void disableBookWithWrongOrigin() {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();


        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", book.getId())
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
    void delete() {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        given(specification)
                .pathParam("id", book.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(10)
    void deleteWithWrongOrigin() {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        given(specification)
                .pathParam("id", book.getId())
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
                .setBasePath("/api/book/v1")
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

        // Lê diretamente a árvore do JSON e extrai a lista do nó "content"
        com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(content);
        com.fasterxml.jackson.databind.JsonNode contentNode = rootNode.path("content");

        // Converte o nó "content" diretamente para a lista de BookDTO compatível com Spring Boot 4
        List<BookDTO> books = objectMapper.readValue(
                contentNode.toString(),
                new com.fasterxml.jackson.core.type.TypeReference<List<BookDTO>>(){}
        );

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
    @Order(12)
    void findAllWithWrongOrigin() {
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/book/v1")
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
    void findByAuthor() throws JsonProcessingException {
        //{{baseUrl}}/api/book/v1/findBooksByAuthor/and?page=0&size=2&direction=asc
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_LOCAL)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("author","and")
                .queryParams("page", 0, "size", 12, "direction", "asc")
                .when()
                .get("findBooksByAuthor/{author}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        // Lê diretamente a árvore do JSON e extrai a lista do nó "content"
        com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(content);
        com.fasterxml.jackson.databind.JsonNode contentNode = rootNode.path("content");

        // Converte o nó "content" diretamente para a lista de BookDTO compatível com Spring Boot 4
        List<BookDTO> books = objectMapper.readValue(
                contentNode.toString(),
                new com.fasterxml.jackson.core.type.TypeReference<List<BookDTO>>(){}
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

    @Test
    @Order(14)
    void findByAuthorWithWrongOrigin() {
        //{{baseUrl}}/api/book/v1/findBooksByAuthor/and?page=0&size=2&direction=asc
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("author","and")
                .queryParams("page", 0, "size", 12, "direction", "asc")
                .when()
                .get("findBooksByAuthor/{author}")
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
        mockBook();
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_ERUDIO)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/book/v1/massCreation")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        byte[] file = """
                  title;author;price;launch_date;enabled
                  Docker Deep Dive - Updated;Nigel Poulton;55.99;2017-11-29 13:50:05.878000;true
                  Clean Code;Robert C. Martin;49.90;2023-06-10 10:30:00.000000;true
            """.getBytes(StandardCharsets.UTF_8);
        var content = given(specification).multiPart("file", "Mockbooks.csv", file, MediaTypes.APPLICATION_CSV_VALUE)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        List<BookDTO> books = objectMapper.readValue(
                content,
                new com.fasterxml.jackson.core.type.TypeReference<List<BookDTO>>(){}
        );

        assertNotNull(books);
        assertEquals(2, books.size());

        assertNotNull(books.get(0).getId());
        assertEquals("Docker Deep Dive - Updated", books.get(0).getTitle());
        assertEquals("Nigel Poulton", books.get(0).getAuthor());
        assertEquals(55.99, books.get(0).getPrice());
        assertNotNull(books.get(0).getLaunchDate());
        assertTrue(books.get(0).getEnabled());

        assertNotNull(books.get(1).getId());
        assertEquals("Clean Code", books.get(1).getTitle());
        assertEquals("Robert C. Martin", books.get(1).getAuthor());
        assertEquals(49.90, books.get(1).getPrice());
        assertNotNull(books.get(1).getLaunchDate());
        assertTrue(books.get(1).getEnabled());
    }

    @Test
    @Order(16)
    void massCreationCsvWithWrongOrigin() throws Exception {
        mockBook();
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .setBasePath("/api/book/v1/massCreation")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        byte[] file = """
                  title;author;price;launch_date;enabled
                  Docker Deep Dive - Updated;Nigel Poulton;55.99;2017-11-29 13:50:05.878000;true
                  Clean Code;Robert C. Martin;49.90;2023-06-10 10:30:00.000000;true
            """.getBytes(StandardCharsets.UTF_8);
        var content = given(specification).multiPart("file", "Mockbooks.csv", file, MediaTypes.APPLICATION_CSV_VALUE)
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
                .addHeader(
                        TestConfigs.HEADER_PARAM_ORIGIN,
                        TestConfigs.ORIGIN_ERUDIO
                )
                .addHeader(
                        TestConfigs.HEADER_PARAM_AUTHORIZATION,
                        "Bearer " + tokenDTO.getAccessToken()
                )
                .setBasePath("/api/book/v1/massCreation")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Books");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("author");
        header.createCell(1).setCellValue("launch_date");
        header.createCell(2).setCellValue("price");
        header.createCell(3).setCellValue("title");

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("Nigel Poulton");
        row1.createCell(1).setCellValue("2017-11-29 13:50:05.878000");
        row1.createCell(2).setCellValue(55.99);
        row1.createCell(3).setCellValue("Docker Deep Dive - Updated");

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("Robert C. Martin");
        row2.createCell(1).setCellValue("2023-06-10 10:30:00.000000");
        row2.createCell(2).setCellValue(49.90);
        row2.createCell(3).setCellValue("Clean Code");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        var content = given(specification)
                .multiPart("file", "Mockbooks.xlsx", outputStream.toByteArray(), MediaTypes.APPLICATION_XLSX_VALUE)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        List<BookDTO> books = objectMapper.readValue(
                content,
                new com.fasterxml.jackson.core.type.TypeReference<List<BookDTO>>(){}

        );

        assertNotNull(books);
        assertEquals(2, books.size());

        assertEquals("Docker Deep Dive - Updated", books.get(0).getTitle());
        assertEquals("Nigel Poulton", books.get(0).getAuthor());
        assertEquals(55.99, books.get(0).getPrice());
        assertNotNull(books.get(0).getLaunchDate());
        assertTrue(books.get(0).getEnabled());

        assertEquals("Clean Code", books.get(1).getTitle());
        assertEquals("Robert C. Martin", books.get(1).getAuthor());
        assertEquals(49.90, books.get(1).getPrice());
        assertNotNull(books.get(1).getLaunchDate());
        assertTrue(books.get(1).getEnabled());
    }
    @Test
    @Order(17)
    void massCreationXlsxWithWrongOrigin() throws Exception {

        specification = new RequestSpecBuilder()
                .addHeader(
                        TestConfigs.HEADER_PARAM_ORIGIN,
                        TestConfigs.ORIGIN_SEMERU
                )
                .addHeader(
                        TestConfigs.HEADER_PARAM_AUTHORIZATION,
                        "Bearer " + tokenDTO.getAccessToken()
                )
                .setBasePath("/api/book/v1/massCreation")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();



        var content = given(specification)
                .multiPart("file", "Mockbooks.xlsx", MediaTypes.APPLICATION_XLSX_VALUE)
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
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_ERUDIO)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .addHeader("Accept", MediaTypes.APPLICATION_CSV_VALUE)
                .setBasePath("/api/book/v1")
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
                .setBasePath("/api/book/v1")
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
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_ERUDIO)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .addHeader("Accept", MediaTypes.APPLICATION_XLSX_VALUE)
                .setBasePath("/api/book/v1")
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
                .setBasePath("/api/book/v1")
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
    @Disabled("Problema específico do ambiente Linux com JasperReports")
    void exportPagePdf() throws JsonProcessingException{
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_ERUDIO)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .addHeader("Accept", MediaTypes.APPLICATION_PDF_VALUE)
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        var content = given(specification)
                .queryParams("page", 3, "size", 12, "direction", "asc")
                .when()
                .get("/exportPage")
                .then()
                .log().all()
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
    void exportPagePdfWithWrongOrigin() throws JsonProcessingException{
        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_SEMERU)
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken())
                .addHeader("Accept", MediaTypes.APPLICATION_PDF_VALUE)
                .setBasePath("/api/book/v1")
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


    }



    private void mockBook() {
        book.setTitle("Docker Deep Dive - Updated");
        book.setAuthor("Nigel Poulton");
        book.setPrice(Double.valueOf(55.99));
        book.setLaunchDate(new Date());
        book.setEnabled(true);
    }






}