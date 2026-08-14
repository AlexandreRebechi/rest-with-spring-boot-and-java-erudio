package com.example.erudio.services;

import com.example.erudio.controllers.BookController;
import com.example.erudio.data.dto.BookDTO;
import com.example.erudio.exception.BadRequestException;
import com.example.erudio.exception.FileStorageException;
import com.example.erudio.exception.RequiredObjectIsNullException;
import com.example.erudio.exception.ResourceNotFoundException;
import com.example.erudio.file.exporter.contract.book.FileExporterBook;
import com.example.erudio.file.exporter.factory.book.FileExporterFactoryBook;
import com.example.erudio.file.importer.contract.book.FileImporterBook;
import com.example.erudio.file.importer.factory.book.FileImporterFactoryBook;
import com.example.erudio.model.Book;
import com.example.erudio.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static com.example.erudio.mapper.ObjectMapper.parseObject;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Service
public class BookServices {

    final AtomicLong counter = new AtomicLong();
    final Logger logger = LoggerFactory.getLogger(BookServices.class.getName());

    @Autowired
    BookRepository repository;

    @Autowired
    FileImporterFactoryBook importer;

    @Autowired
    FileExporterFactoryBook exporter;

    @Autowired
    PagedResourcesAssembler<BookDTO> assembler;

    public PagedModel<EntityModel<BookDTO>> findAll(Pageable pageable) {
        logger.debug("Finding findAll()");

        var books = repository.findAll(pageable);

        return buildPagedModel(pageable, books);

    }

    public PagedModel<EntityModel<BookDTO>> findByAuthor(String author,Pageable pageable) {
        logger.info("Finding Books by author");

        var books = repository.findBooksByAuthor(author, pageable);
        return buildPagedModel(pageable, books);

    }

    public BookDTO findById(long id) {
        logger.info("Finding one Book!");
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID"));
        var dto = parseObject(entity, BookDTO.class);
        addHateoasLinks(dto);
        return dto;

    }
    public Resource exportPage(Pageable pageable, String acceptHeader) {
        logger.info("Exporting a Books page!");

        var books = repository.findAll(pageable)
                .map(book -> parseObject(book, BookDTO.class))
                .getContent();

        try {
            FileExporterBook exporter = this.exporter.getExporter(acceptHeader);
            return exporter.exportFile(books);
        } catch (Exception e) {
            throw new RuntimeException("Error during export!", e);
        }
    }


        public BookDTO create(BookDTO book) {

        if (book == null) throw new RequiredObjectIsNullException();

        logger.info("Creating one Book!");
        var entity = parseObject(book, Book.class);
        var dto = parseObject(repository.save(entity), BookDTO.class);
        addHateoasLinks(dto);
        return dto;
    }

    public List<BookDTO> massCreation(MultipartFile file) {

        logger.info("Importing Books from file");

        if (file.isEmpty()) {
            throw new BadRequestException("Please set a Valid File!");
        }

        try (InputStream inputStream = file.getInputStream()) {

            String filename = Optional.ofNullable(file.getOriginalFilename())
                    .orElseThrow(() ->
                            new BadRequestException("File name cannot be null"));

            logger.info("Processing file: {}", filename);

            FileImporterBook importer = this.importer.getImporter(filename);

            logger.info("Importer selected: {}", importer.getClass().getSimpleName());

            List<BookDTO> importedBooks = importer.importFile(inputStream);

            logger.info("Books imported from file: {}", importedBooks.size());

            List<Book> entities = importedBooks.stream()
                    .map(dto -> repository.save(
                            parseObject(dto, Book.class)
                    ))
                    .toList();

            logger.info("Books saved in database: {}", entities.size());

            return entities.stream()
                    .map(entity -> {
                        var dto = parseObject(entity, BookDTO.class);
                        addHateoasLinks(dto);
                        return dto;
                    })
                    .toList();

        } catch (Exception e) {

            logger.error("Error processing file!", e);

            throw new FileStorageException(
                    "Error processing the file!",
                    e
            );
        }
    }


    public BookDTO update(BookDTO book) {

        if (book == null) throw new RequiredObjectIsNullException();

        logger.info("Updating one Book!");
        Book entity = repository.findById(book.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID"));

        entity.setAuthor(book.getAuthor());
        entity.setLaunchDate(book.getLaunchDate());
        entity.setPrice(book.getPrice());
        entity.setTitle(book.getTitle());

        var dto = parseObject(repository.save(entity), BookDTO.class);
        addHateoasLinks(dto);
        return dto;
    }

    @Transactional
    public BookDTO disableBook(Long id) {

        logger.info("Disabling one Book!");

        repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        repository.disableBook(id);

        var entity = repository.findById(id).get();
        var dto = parseObject(entity, BookDTO.class);
        addHateoasLinks(dto);
        return dto;
    }

    public void delete(Long id) {
        logger.info("Deleting one Book!");
        Book entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID"));

        repository.delete(entity);
    }

    private PagedModel<EntityModel<BookDTO>> buildPagedModel(Pageable pageable, Page<Book> books) {
        var booksWithLinks = books.map(book -> {
            var dto = parseObject(book, BookDTO.class);
            addHateoasLinks(dto);
            return dto;

        });

        Link findAllLink = WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(BookController.class)
                                .findAll(
                                        pageable.getPageNumber(),
                                        pageable.getPageSize(),
                                        String.valueOf(pageable.getSort())))
                .withSelfRel();
        return assembler.toModel(booksWithLinks, findAllLink);


    }

    private void addHateoasLinks(BookDTO dto) {
        dto.add(linkTo(methodOn(BookController.class).findById(dto.getId())).withSelfRel().withType("GET"));
        dto.add(linkTo(methodOn(BookController.class).findAll(1, 12, "asc")).withRel("findAll").withType("GET"));
        dto.add(linkTo(methodOn(BookController.class).create(dto)).withRel("create").withType("POST"));
        dto.add(linkTo(methodOn(BookController.class).update(dto)).withRel("update").withType("PUT"));
        dto.add(linkTo(methodOn(BookController.class).disableBook(dto.getId())).withRel("disable").withType("PATCH"));
        dto.add(linkTo(methodOn(BookController.class).delete(dto.getId())).withRel("delete").withType("DELETE"));
        dto.add(linkTo(methodOn(BookController.class)
                .exportPage(
                        1, 12, "asc", null))
                .withRel("exportPage")
                .withType("GET")
                .withTitle("Export Books")
        );
    }

}