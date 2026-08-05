package com.example.erudio.repository;

import com.example.erudio.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Book b SET b.enabled = false WHERE b.id =:id")
    void disableBook(@Param("id") Long id);

    @Query("SELECT b FROM Book b WHERE b.author LIKE LOWER(CONCAT ('%',:author, '%'))")
    Page<Book> findBooksByAuthor(@Param("author") String author, Pageable pageable);
}
