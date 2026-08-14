package com.example.erudio.repository;

import com.example.erudio.integrationtests.testcontainers.AbstractIntegrationTest;
import com.example.erudio.model.Book;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private BookRepository repository;
    private static Book book;

    @BeforeAll
    static void setUp() {
        book = new Book();
    }

    @Test
    @Order(1)
    void findBooksByAuthor() {
        Pageable pageable = PageRequest.of(
                0,
                12,
                Sort.by(Sort.Direction.ASC, "author"));

        book = repository.findBooksByAuthor("ina", pageable).getContent().get(0);

        assertNotNull(book.getId());
        assertNotNull(book.getTitle());
        assertNotNull(book.getAuthor());
        assertNotNull(book.getPrice());
        assertTrue(book.getId() > 0);
        assertEquals("Aguinaldo Aragon Fernandes e Vladimir Ferraz de Abreu", book.getAuthor());
        assertEquals("Implantando a governança de TI", book.getTitle());
        assertEquals(54.0, book.getPrice());
        assertTrue(book.getEnabled());

    }

    @Test
    @Order(2)
    void disableBook() {

        long id = book.getId();
        repository.disableBook(id);

        var result = repository.findById(id);
        book = result.get();

        assertNotNull(book.getId());
        assertNotNull(book.getTitle());
        assertNotNull(book.getAuthor());
        assertNotNull(book.getPrice());
        assertTrue(book.getId() > 0);
        assertEquals("Aguinaldo Aragon Fernandes e Vladimir Ferraz de Abreu", book.getAuthor());
        assertEquals("Implantando a governança de TI", book.getTitle());
        assertEquals(54.0, book.getPrice());
        assertFalse(book.getEnabled());


    }
}