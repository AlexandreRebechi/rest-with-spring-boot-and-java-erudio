package com.example.erudio.services;

import com.example.erudio.data.dto.BookDTO;
import com.example.erudio.data.dto.PersonDTO;
import com.example.erudio.exception.RequiredObjectIsNullException;
import com.example.erudio.file.exporter.contract.book.FileExporterBook;
import com.example.erudio.file.exporter.factory.book.FileExporterFactoryBook;
import com.example.erudio.file.importer.contract.book.FileImporterBook;
import com.example.erudio.file.importer.factory.book.FileImporterFactoryBook;
import com.example.erudio.model.Book;
import com.example.erudio.repository.BookRepository;
import com.example.erudio.unittests.mapper.mocks.MockBook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    MockBook input;

    @InjectMocks
    private BookService service;

    @Mock
    BookRepository repository;

    @Mock
    PagedResourcesAssembler<BookDTO> assembler;

    @Mock
    private FileImporterFactoryBook importer;

    @Mock
    private FileImporterBook fileImporter;

    @Mock
    private FileExporterFactoryBook exporter;

    @Mock
    private FileExporterBook fileExporter;

    @BeforeEach
    void setUp() {
        input = new MockBook();
    }


    @Test
    void findAll() {
        // Mocking repository access
        List<Book> mockEntityList = input.mockEntityList();
        Page<Book> mockPage = new PageImpl<>(mockEntityList);
        when(repository.findAll(any(Pageable.class))).thenReturn(mockPage);

        List<BookDTO> mockDtoList = input.mockDTOList();

        /*Mocking assembler
        assembler.toModel(booksWithLinks, findAllLink);*/
        List<EntityModel<BookDTO>> entityModels = mockDtoList.stream()
                .map(EntityModel::of)
                .collect(Collectors.toList());

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(
                mockPage.getSize(),
                mockPage.getNumber(),
                mockPage.getTotalElements(),
                mockPage.getTotalPages()
        );

        PagedModel<EntityModel<BookDTO>> mockPagedModel = PagedModel.of(entityModels, pageMetadata);
        when(assembler.toModel(any(Page.class), any(Link.class))).thenReturn(mockPagedModel);


        // Executing fid all
        PagedModel<EntityModel<BookDTO>> result = service.findAll(PageRequest.of(0, 14));

        List<BookDTO> books = result.getContent()
                .stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toList());

        assertNotNull(books);
        assertEquals(14, books.size());

        validateIndividualBook(books.get(1), 1);
        validateIndividualBook(books.get(4), 4);
        validateIndividualBook(books.get(7), 7);
    }

    @Test
    void findById() {
        Book book = input.mockEntity(1);
        book.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(book));

        var result = service.findById(1L);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getLinks());

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("self")
                        && link.getHref().endsWith("/api/book/v1/1")
                        && link.getType().equals("GET")
                ));

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("findAll")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("GET")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("POST")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("update")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("PUT")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("delete")
                        && link.getHref().endsWith("/api/book/v1/1")
                        && link.getType().equals("DELETE")
                )
        );

        assertEquals("Some Author1", result.getAuthor());
        assertEquals(25D, result.getPrice());
        assertEquals("Some Title1", result.getTitle());
        assertNotNull(result.getLaunchDate());
    }

    @Test
    void findBooksByAuthor() {
        List<Book> mockEntityList = input.mockEntityList();
        Page<Book> mockPage = new PageImpl<>(mockEntityList);
        when(repository.findBooksByAuthor(eq("First Author Test"),any(Pageable.class))).thenReturn(mockPage);

        List<BookDTO> mockDtoList = input.mockDTOList();

        // Mocking assembler
        // assembler.toModel(peopleWithLinks, findAllLink);
        List<EntityModel<BookDTO>> entityModels = mockDtoList.stream()
                .map(EntityModel::of)
                .collect(Collectors.toList());

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(
                mockPage.getSize(),
                mockPage.getNumber(),
                mockPage.getTotalElements(),
                mockPage.getTotalPages()
        );

        PagedModel<EntityModel<BookDTO>> mockPagedModel = PagedModel.of(entityModels, pageMetadata);
        when(assembler.toModel(any(Page.class), any(Link.class))).thenReturn(mockPagedModel);


        // Executing fid all
        PagedModel<EntityModel<BookDTO>> result = service.findByAuthor("First Author Test", PageRequest.of(0, 14));

        List<BookDTO> books = result.getContent()
                .stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toList());

        assertNotNull(books);
        assertEquals(14, books.size());

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("self")
                        && link.getHref().endsWith("/api/book/v1/1")
                        && link.getType().equals("GET")
                ));

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("findAll")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("GET")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("POST")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("update")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("PUT")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("delete")
                        && link.getHref().endsWith("/api/person/v1/1")
                        && link.getType().equals("DELETE")
                )
        );

        validateIndividualBook(books.get(1), 1);
        validateIndividualBook(books.get(4), 4);
        validateIndividualBook(books.get(7), 7);

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("self")
                        && link.getHref().endsWith("/api/book/v1/1")
                        && link.getType().equals("GET")
                ));

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("findAll")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("GET")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("POST")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("update")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("PUT")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("delete")
                        && link.getHref().endsWith("/api/book/v1/1")
                        && link.getType().equals("DELETE")
                )
        );
    }
    @Test
    void exportPageCSV() throws Exception {

        List<Book> persons = input.mockEntityList();

        Pageable pageable = PageRequest.of(0, 14);

        Page<Book> page = new PageImpl<>(
                persons,
                pageable,
                persons.size()
        );

        Resource resource = new ByteArrayResource(
                "test file".getBytes()
        );

        when(repository.findAll(pageable))
                .thenReturn(page);

        when(exporter.getExporter("text/csv"))
                .thenReturn(fileExporter);

        when(fileExporter.exportBooks(anyList()))
                .thenReturn(resource);

        var result = service.exportPage(
                pageable,
                "text/csv"
        );

        assertNotNull(result);
        assertEquals(resource, result);

        verify(repository).findAll(pageable);
        verify(exporter).getExporter("text/csv");
        verify(fileExporter).exportBooks(anyList());
    }

    @Test
    void exportPageXlsx() throws Exception {

        List<Book> persons = input.mockEntityList();

        Pageable pageable = PageRequest.of(0, 14);

        Page<Book> page = new PageImpl<>(
                persons,
                pageable,
                persons.size()
        );

        Resource resource = new ByteArrayResource(
                "test file".getBytes()
        );

        when(repository.findAll(pageable))
                .thenReturn(page);

        when(exporter.getExporter(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )).thenReturn(fileExporter);

        when(fileExporter.exportBooks(anyList()))
                .thenReturn(resource);

        var result = service.exportPage(
                pageable,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        assertNotNull(result);
        assertEquals(resource, result);

        verify(repository).findAll(pageable);

        verify(exporter).getExporter(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        verify(fileExporter).exportBooks(anyList());
    }

    @Test
    void exportPagePdf() throws Exception {

        List<Book> persons = input.mockEntityList();

        Pageable pageable = PageRequest.of(0, 14);

        Page<Book> page = new PageImpl<>(
                persons,
                pageable,
                persons.size()
        );

        Resource resource = new ByteArrayResource(
                "test file".getBytes()
        );

        when(repository.findAll(pageable))
                .thenReturn(page);

        when(exporter.getExporter("application/pdf"))
                .thenReturn(fileExporter);

        when(fileExporter.exportBooks(anyList()))
                .thenReturn(resource);

        var result = service.exportPage(
                pageable,
                "application/pdf"
        );

        assertNotNull(result);
        assertEquals(resource, result);

        verify(repository).findAll(pageable);
        verify(exporter).getExporter("application/pdf");
        verify(fileExporter).exportBooks(anyList());
    }

    @Test
    void create() {

        BookDTO dto = input.mockDTO(1);

        Book entity = input.mockEntity(1);

        when(repository.save(any(Book.class))).thenReturn(entity);

        var result = service.create(dto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getLinks());

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("self")
                        && link.getHref().endsWith("/api/book/v1/1")
                        && link.getType().equals("GET")
                ));

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("findAll")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("GET")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("POST")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("update")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("PUT")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("delete")
                        && link.getHref().endsWith("/api/book/v1/1")
                        && link.getType().equals("DELETE")
                )
        );

        assertEquals("Some Author1", result.getAuthor());
        assertEquals(25D, result.getPrice());
        assertEquals("Some Title1", result.getTitle());
        assertNotNull(result.getLaunchDate());
    }

    @Test
    void massCreationCsv() throws Exception {
        String content = """
               author,price,title,launch_date
                       Some Author1,25.0,Some Title1,2024-01-15
                       Some Author2,30.0,Some Title2,2024-02-20
            """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "books.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );

        List<BookDTO> books = List.of(
                input.mockDTO(1),
                input.mockDTO(2)
        );

        when(importer.getImporter("books.csv"))
                .thenReturn(fileImporter);

        when(fileImporter.importFile(any(InputStream.class)))
                .thenReturn(books);

        AtomicLong id = new AtomicLong(1);

        when(repository.save(any(Book.class)))
                .thenAnswer(invocation -> {
                    Book book = invocation.getArgument(0);
                    book.setId(id.getAndIncrement());
                    return book;
                });

        var result = service.massCreation(file);

        assertNotNull(result);
        assertEquals(2, result.size());
        var book = result.get(1);

        assertNotNull(book.getLinks());
        book.getLinks().forEach(link ->
                System.out.println(
                        "REL = " + link.getRel().value()
                                + " | HREF = " + link.getHref()
                                + " | TYPE = " + link.getType()
                )
        );
        validateLinks(result.get(0));
        assertEquals("Some Author1", result.get(0).getAuthor());
        assertEquals(25D, result.get(0).getPrice());
        assertEquals("Some Title1", result.get(0).getTitle());
        assertNotNull(result.get(0).getLaunchDate());

        validateLinks(result.get(1));
        assertEquals("Some Author2", result.get(1).getAuthor());
        assertEquals(25D, result.get(1).getPrice());
        assertEquals("Some Title2", result.get(1).getTitle());
        assertNotNull(result.get(1).getLaunchDate());

        assertNotNull(result.get(0).getId());
        assertNotNull(result.get(1).getId());

        verify(importer).getImporter("books.csv");
        verify(fileImporter).importFile(any(InputStream.class));
        verify(repository, times(2)).save(any(Book.class));
    }

    @Test
    void massCreationXlsx() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "books.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test file".getBytes()
        );

        List<BookDTO> books = List.of(
                input.mockDTO(1),
                input.mockDTO(2)
        );

        when(importer.getImporter("books.xlsx"))
                .thenReturn(fileImporter);

        when(fileImporter.importFile(any(InputStream.class)))
                .thenReturn(books);

        AtomicLong id = new AtomicLong(1);

        when(repository.save(any(Book.class)))
                .thenAnswer(invocation -> {
                    Book book = invocation.getArgument(0);
                    book.setId(id.getAndIncrement());
                    return book;
                });

        var result = service.massCreation(file);

        assertNotNull(result);
        assertEquals(2, result.size());
        var book = result.get(1);

        assertNotNull(book.getLinks());
        book.getLinks().forEach(link ->
                System.out.println(
                        "REL = " + link.getRel().value()
                                + " | HREF = " + link.getHref()
                                + " | TYPE = " + link.getType()
                )
        );

        validateLinks(result.get(0));
        assertEquals("Some Author1", result.get(0).getAuthor());
        assertEquals(25D, result.get(0).getPrice());
        assertEquals("Some Title1", result.get(0).getTitle());
        assertNotNull(result.get(0).getLaunchDate());

        validateLinks(result.get(1));
        assertEquals("Some Author2", result.get(1).getAuthor());
        assertEquals(25D, result.get(1).getPrice());
        assertEquals("Some Title2", result.get(1).getTitle());
        assertNotNull(result.get(1).getLaunchDate());

        assertNotNull(result.get(0).getId());
        assertNotNull(result.get(1).getId());

        verify(importer).getImporter("books.xlsx");
        verify(fileImporter).importFile(any(InputStream.class));
        verify(repository, times(2)).save(any(Book.class));
    }


    @Test
    void testCreateWithNullBook() {
        Exception exception = assertThrows(RequiredObjectIsNullException.class,
                () -> {
                    service.create(null);
                });

        String expectedMessage = "It is not allowed to persist a null object!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void update() {
        Book book = input.mockEntity(1);
        Book persisted = book;
        persisted.setId(1L);

        BookDTO dto = input.mockDTO(1);

        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(repository.save(book)).thenReturn(persisted);

        var result = service.update(dto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getLinks());

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("self")
                        && link.getHref().endsWith("/api/book/v1/1")
                        && link.getType().equals("GET")
                ));

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("findAll")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("GET")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("POST")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("update")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("PUT")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("delete")
                        && link.getHref().endsWith("/api/book/v1/1")
                        && link.getType().equals("DELETE")
                )
        );

        assertEquals("Some Author1", result.getAuthor());
        assertEquals(25D, result.getPrice());
        assertEquals("Some Title1", result.getTitle());
        assertNotNull(result.getLaunchDate());
    }

    @Test
    void testUpdateWithNullBook() {
        Exception exception = assertThrows(RequiredObjectIsNullException.class,
                () -> {
                    service.update(null);
                });

        String expectedMessage = "It is not allowed to persist a null object!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void delete() {
        Book book = input.mockEntity(1);
        book.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(book));

        service.delete(1L);
        verify(repository, times(1)).findById(anyLong());
        verify(repository, times(1)).delete(any(Book.class));
        verifyNoMoreInteractions(repository);
    }

    private static void validateIndividualBook(BookDTO book, int i) {
        assertNotNull(book);
        assertNotNull(book.getId());
        assertNotNull(book.getLinks());

        assertNotNull(book.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("self")
                        && link.getHref().endsWith("/api/book/v1/" + i)
                        && link.getType().equals("GET")
                ));

        assertNotNull(book.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("findAll")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("GET")
                )
        );

        assertNotNull(book.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("POST")
                )
        );

        assertNotNull(book.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("update")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("PUT")
                )
        );

        assertNotNull(book.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("delete")
                        && link.getHref().endsWith("/api/book/v1/" + i)
                        && link.getType().equals("DELETE")
                )
        );

        assertEquals("Some Author" + i, book.getAuthor());
        assertEquals(25D, book.getPrice());
        assertEquals("Some Title" + i, book.getTitle());
        assertNotNull(book.getLaunchDate());
    }
    private void validateLinks(BookDTO book) {

        assertNotNull(book.getLinks());
        System.out.println("\n===== BOOK ID: " + book.getId() + " =====");
        book.getLinks().forEach(link ->
                System.out.println(
                        "REL = " + link.getRel().value()
                                + " | HREF = " + link.getHref()
                                + " | TYPE = " + link.getType()
                )
        );

        book.getLinks().forEach(link ->
                System.out.println(
                        "REL = " + link.getRel().value()
                                + " | HREF = " + link.getHref()
                                + " | TYPE = " + link.getType()
                )
        );
        assertTrue(book.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("self")
                                && link.getHref().endsWith(
                                "/api/book/v1/" + book.getId()
                        )
                                && link.getType().equals("GET")
                ));

        assertTrue(book.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("findAll")
                        && link.getHref().startsWith("http://localhost:8888/api/book/v1")
                        && "GET".equals(link.getType())
                ));

        assertTrue(book.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("POST")
                ));

        assertTrue(book.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("massCreation")
                        && link.getHref().endsWith("/api/book/v1/massCreation")
                        && link.getType().equals("POST")
                ));

        assertTrue(book.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("update")
                        && link.getHref().endsWith("/api/book/v1")
                        && link.getType().equals("PUT")
                ));

        assertTrue(book.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("delete")
                                && link.getHref().endsWith(
                                "/api/book/v1/" + book.getId()
                        )
                                && link.getType().equals("DELETE")
                ));
    }


}