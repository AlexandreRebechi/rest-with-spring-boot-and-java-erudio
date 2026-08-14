package com.example.erudio.services;

import com.example.erudio.controllers.PersonController;
import com.example.erudio.data.dto.PersonDTO;
import com.example.erudio.exception.BadRequestException;
import com.example.erudio.exception.FileStorageException;
import com.example.erudio.exception.ResourceNotFoundException;
import com.example.erudio.exception.RequiredObjectIsNullException;

import com.example.erudio.file.exporter.MediaTypes;
import com.example.erudio.file.exporter.contract.person.FileExporterPerson;
import com.example.erudio.file.exporter.factory.person.FileExporterFactoryPerson;
import com.example.erudio.file.importer.contract.person.FileImporterPerson;
import com.example.erudio.file.importer.factory.person.FileImporterFactoryPerson;
import com.example.erudio.model.Person;
import com.example.erudio.repository.PersonRepository;
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
public class PersonServices {

    private final AtomicLong counter  = new AtomicLong();
    private Logger logger = LoggerFactory.getLogger(PersonServices.class.getName());

    @Autowired
    PersonRepository repository;

    @Autowired
    FileImporterFactoryPerson importer;

    @Autowired
    FileExporterFactoryPerson exporter;

    @Autowired
    PagedResourcesAssembler<PersonDTO> assembler;



    public PagedModel<EntityModel<PersonDTO>> findAll(Pageable pageable) {
        logger.info("Finding all Persons");

        var people = repository.findAll(pageable);
        return buildPagedModel(pageable, people);

    }



    public PagedModel<EntityModel<PersonDTO>> findByName(String firstName,Pageable pageable) {
        logger.info("Finding People by name");

        var people = repository.findPeopleByName(firstName, pageable);
        return buildPagedModel(pageable, people);

    }


    public PersonDTO findById(Long id){
        logger.info("Finding one Person!");

        var entity =  repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID"));

        var dto = parseObject(entity, PersonDTO.class);
        addHateoasLinks(dto);
        return dto;
    }

    public Resource exportPage(Pageable pageable, String acceptHeader) {
        logger.info("Exporting a People page!");

        var people = repository.findAll(pageable)
                .map(person ->parseObject(person, PersonDTO.class))
                .getContent();

        try {
            FileExporterPerson exporter = this.exporter.getExporter(acceptHeader);
            return exporter.exportFile(people);
        } catch (Exception e) {
            throw new RuntimeException("Error during export!", e);
        }

    }

    public PersonDTO create(PersonDTO person){

        if(person == null) throw new RequiredObjectIsNullException();

        logger.info("Creating one Person!");
        var entity = parseObject(person, Person.class);
        var dto = parseObject(repository.save(entity), PersonDTO.class);
        addHateoasLinks(dto);
        return dto;
    }

    public List<PersonDTO> massCreation(MultipartFile file) {

        logger.info("Importing Persons from file");

        if (file.isEmpty()) {
            throw new BadRequestException("Please set a Valid File!");
        }

        try (InputStream inputStream = file.getInputStream()) {

            String filename = Optional.ofNullable(file.getOriginalFilename())
                    .orElseThrow(() ->
                            new BadRequestException("File name cannot be null"));

            logger.info("Processing file: {}", filename);

            FileImporterPerson importer = this.importer.getImporter(filename);

            logger.info("Importer selected: {}", importer.getClass().getSimpleName());

            List<PersonDTO> importedPeople = importer.importFile(inputStream);

            logger.info("People imported from file: {}", importedPeople.size());

            List<Person> entities = importedPeople.stream()
                    .map(dto -> repository.save(
                            parseObject(dto, Person.class)
                    ))
                    .toList();

            logger.info("People saved in database: {}", entities.size());

            return entities.stream()
                    .map(entity -> {
                        var dto = parseObject(entity, PersonDTO.class);
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

    public PersonDTO update(PersonDTO person){

        if(person == null) throw new RequiredObjectIsNullException();

        logger.info("Updating one Person!");
       Person entity = repository.findById(person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID"));

        entity.setFirstName(person.getFirstName());
        entity.setLastName(person.getLastName());
        entity.setAddress(person.getAddress());
        entity.setGender(person.getGender());

        var dto = parseObject(repository.save(entity), PersonDTO.class);
        addHateoasLinks(dto);
        return dto;
    }

    @Transactional
    public PersonDTO disablePerson(Long id) {

        logger.info("Disabling one Person!");

        repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        repository.disablePerson(id);

        var entity = repository.findById(id).get();
        var dto = parseObject(entity, PersonDTO.class);
        addHateoasLinks(dto);
        return dto;
    }

    public void delete(Long id){
        logger.info("Deleting one Person!");
       Person entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID"));

       repository.delete(entity);

    }

    private PagedModel<EntityModel<PersonDTO>> buildPagedModel(Pageable pageable, Page<Person> people) {
        var peopleWithLinks = people.map(person -> {
            var dto = parseObject(person, PersonDTO.class);
            addHateoasLinks(dto);
            return dto;

        });

        Link findAllLink = WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(PersonController.class)
                                .findAll(
                                        pageable.getPageNumber(),
                                        pageable.getPageSize(),
                                        String.valueOf(pageable.getSort())))
                .withSelfRel();
        return assembler.toModel(peopleWithLinks, findAllLink);
    }

    private void addHateoasLinks(PersonDTO dto) {
        dto.add(linkTo(methodOn(PersonController.class).findById(dto.getId())).withSelfRel().withType("GET"));
        dto.add(linkTo(methodOn(PersonController.class).findAll(1, 12, "asc")).withRel("findAll").withType("GET"));
        dto.add(linkTo(methodOn(PersonController.class).findByName("",1, 12, "asc")).withRel("findByName").withType("GET"));
        dto.add(linkTo(methodOn(PersonController.class).create(dto)).withRel("create").withType("POST"));
        dto.add(linkTo(methodOn(PersonController.class)).slash("massCreation").withRel("massCreation").withType("POST"));
        dto.add(linkTo(methodOn(PersonController.class).update(dto)).withRel("update").withType("PUT"));
        dto.add(linkTo(methodOn(PersonController.class).disablePerson(dto.getId())).withRel("disable").withType("PATCH"));
        dto.add(linkTo(methodOn(PersonController.class).delete(dto.getId())).withRel("delete").withType("DELETE"));
        dto.add(linkTo(methodOn(PersonController.class)
                .exportPage(
                1, 12, "asc", null))
                .withRel("exportPage")
                .withType("GET")
                    .withTitle("Export People")
        );
    }


}
