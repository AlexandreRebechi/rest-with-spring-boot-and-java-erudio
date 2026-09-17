package com.example.erudio.integrationtests.controllers.fileTest;

import com.example.erudio.config.TestConfigs;
import com.example.erudio.controllers.FileController;
import com.example.erudio.integrationtests.dto.AccountCredentialsDTO;
import com.example.erudio.integrationtests.dto.EmailRequestDTO;
import com.example.erudio.integrationtests.dto.TokenDTO;
import com.example.erudio.integrationtests.testcontainers.AbstractIntegrationTest;
import com.example.erudio.services.FileStorageService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileControllerTest{

    @InjectMocks
    private static FileController controller;

    @Mock
    private static FileStorageService service;

    @Mock
    private static HttpServletRequest request;

    @Mock
    private static ServletContext servletContext;

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
    void uploadFile() {

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8888);

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes(StandardCharsets.UTF_8)
        );

        when(service.storeFile(file))
                .thenReturn("test.txt");

        var result = controller.uploadFile(file);

        assertNotNull(result);

        assertEquals(
                "test.txt",
                result.getFileName()
        );

        assertEquals(
                "text/plain",
                result.getFileType()
        );

        assertEquals(
                file.getSize(),
                result.getSize()
        );

        assertNotNull(result.getFileDownloadUrl());

        assertEquals("http://localhost:8888/api/file/v1/downloadFile/test.txt", result.getFileDownloadUrl());

        verify(service).storeFile(file);

        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @Order(2)
    void uploadMultipleFile() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8888);
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "test1.txt",
                "text/plain",
                "File 1".getBytes(StandardCharsets.UTF_8)
        );
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "test2.txt",
                "text/plain",
                "File 2".getBytes(StandardCharsets.UTF_8)
        );

        when(service.storeFile(file1))
                .thenReturn("test1.txt");

        when(service.storeFile(file2))
                .thenReturn("test2.txt");

        var result = controller.uploadMultipleFile(
                new MultipartFile[]{file1, file2}
        );

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("test1.txt", result.get(0).getFileName());
        assertEquals("test2.txt", result.get(1).getFileName());

        verify(service).storeFile(file1);
        verify(service).storeFile(file2);
    }

    @Test
    @Order(3)
    void downloadFile() throws Exception {
        Path tempDirectory = Files.createTempDirectory("file-test");
        Path filePath = tempDirectory.resolve("test.txt");

        Files.writeString(filePath, "Hello World");

        Resource resource = new FileSystemResource(filePath);

        when(service.loadFileAsResource("test.txt"))
                .thenReturn(resource);

        when(request.getServletContext())
                .thenReturn(servletContext);

        when(servletContext.getMimeType(
                filePath.toAbsolutePath().toString()
        )).thenReturn("text/plain");

        var result = controller.downloadFile(
                "test.txt",
                request
        );

        assertNotNull(result);

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertEquals(
                resource,
                result.getBody()
        );

        assertEquals(
                "text/plain",
                result.getHeaders()
                        .getContentType()
                        .toString()
        );

        String contentDisposition = result.getHeaders()
                .getFirst(HttpHeaders.CONTENT_DISPOSITION);

        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("test.txt"));

        verify(service).loadFileAsResource("test.txt");

        Files.deleteIfExists(filePath);
        Files.deleteIfExists(tempDirectory);
    }
}
