package com.example.erudio.repository;

import com.example.erudio.integrationtests.testcontainers.AbstractIntegrationTest;
import com.example.erudio.model.Book;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private BookRepository repository;

    @Test
    @DisplayName("Deve encontrar livros pelo nome do autor com paginação")
    void findBooksByAuthor() {
        Pageable pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.ASC, "author"));

        var books = repository.findBooksByAuthor("ina", pageable).getContent();

        assertFalse(books.isEmpty(), "A lista de livros não deveria estar vazia");
        Book book = books.get(0);

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
    @DisplayName("Deve desabilitar um livro com sucesso")
    void disableBook() {
        // Busca o livro diretamente para garantir que o ID existe no escopo deste teste
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "author"));
        var books = repository.findBooksByAuthor("ina", pageable).getContent();
        assertFalse(books.isEmpty());
        long id = books.get(0).getId();

        // Executa a operação de modificação
        repository.disableBook(id);

        // Valida o resultado isolado
        var result = repository.findById(id);
        assertTrue(result.isPresent(), "O livro deveria ser encontrado");
        Book bookModificado = result.get();

        assertNotNull(bookModificado.getId());
        assertEquals("Aguinaldo Aragon Fernandes e Vladimir Ferraz de Abreu", bookModificado.getAuthor());
        assertEquals("Implantando a governança de TI", bookModificado.getTitle());
        assertEquals(54.0, bookModificado.getPrice());
        assertFalse(bookModificado.getEnabled(), "O campo enabled deveria ser false");
    }
}