package com.example.erudio.services;

import com.example.erudio.data.dto.PersonDTO;
import com.example.erudio.exception.RequiredObjectIsNullException;
import com.example.erudio.file.exporter.MediaTypes;
import com.example.erudio.file.exporter.contract.person.FileExporterPerson;
import com.example.erudio.file.exporter.factory.person.FileExporterFactoryPerson;
import com.example.erudio.file.importer.contract.person.FileImporterPerson;
import com.example.erudio.file.importer.factory.person.FileImporterFactoryPerson;
import com.example.erudio.model.Person;
import com.example.erudio.repository.PersonRepository;
import com.example.erudio.unittests.mapper.mocks.MockPerson;
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
class PersonServiceTest {

    MockPerson input;

    @InjectMocks
    private PersonService service;

    @Mock
    PersonRepository repository;

    @Mock
    PagedResourcesAssembler<PersonDTO> assembler;

    @Mock
    private FileImporterFactoryPerson importer;

    @Mock
    private FileImporterPerson fileImporter;

    @Mock
    private FileExporterFactoryPerson exporter;

    @Mock
    private FileExporterPerson fileExporter;

    @BeforeEach
    void setUp() {
        input = new MockPerson();
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void findAll() {

        // Mocking repository access
        List<Person> mockEntityList = input.mockEntityList();
        Page<Person> mockPage = new PageImpl<>(mockEntityList);
        when(repository.findAll(any(Pageable.class))).thenReturn(mockPage);

        List<PersonDTO> mockDtoList = input.mockDTOList();

        // Mocking assembler
        // assembler.toModel(peopleWithLinks, findAllLink);
        List<EntityModel<PersonDTO>> entityModels = mockDtoList.stream()
                .map(EntityModel::of)
                .collect(Collectors.toList());

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(
                mockPage.getSize(),
                mockPage.getNumber(),
                mockPage.getTotalElements(),
                mockPage.getTotalPages()
        );

        PagedModel<EntityModel<PersonDTO>> mockPagedModel = PagedModel.of(entityModels, pageMetadata);
        when(assembler.toModel(any(Page.class), any(Link.class))).thenReturn(mockPagedModel);


        // Executing fid all
        PagedModel<EntityModel<PersonDTO>> result = service.findAll(PageRequest.of(0, 14));

        List<PersonDTO> people = result.getContent()
                .stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toList());

        assertNotNull(people);
        assertEquals(14, people.size());

        validateIndividualPerson(people.get(1), 1);
        validateIndividualPerson(people.get(4), 4);
        validateIndividualPerson(people.get(7), 7);
    }

    @Test
    void findById() {
        Person person = input.mockEntity(1);
        person.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(person));

        var result = service.findById(1L);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getLinks());

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("self")
                        && link.getHref().endsWith("/api/person/v1/1")
                        && link.getType().equals("GET")
                ));

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("findAll")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("GET")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("POST")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("update")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("PUT")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("delete")
                        && link.getHref().endsWith("/api/person/v1/1")
                        && link.getType().equals("DELETE")
                )
        );

        assertEquals("Address Test1", result.getAddress());
        assertEquals("First Name Test1", result.getFirstName());
        assertEquals("Last Name Test1", result.getLastName());
        assertEquals("Female", result.getGender());
        assertEquals("Profile Url Test1", person.getProfileUrl());
        assertEquals("Profile Url Test1", person.getProfileUrl());
    }

    @Test
    void findByName() {
        List<Person> mockEntityList = input.mockEntityList();
        Page<Person> mockPage = new PageImpl<>(mockEntityList);
        when(repository.findPeopleByName(eq("First Name Test"),any(Pageable.class))).thenReturn(mockPage);

        List<PersonDTO> mockDtoList = input.mockDTOList();

        // Mocking assembler
        // assembler.toModel(peopleWithLinks, findAllLink);
        List<EntityModel<PersonDTO>> entityModels = mockDtoList.stream()
                .map(EntityModel::of)
                .collect(Collectors.toList());

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(
                mockPage.getSize(),
                mockPage.getNumber(),
                mockPage.getTotalElements(),
                mockPage.getTotalPages()
        );

        PagedModel<EntityModel<PersonDTO>> mockPagedModel = PagedModel.of(entityModels, pageMetadata);
        when(assembler.toModel(any(Page.class), any(Link.class))).thenReturn(mockPagedModel);


        // Executing fid all
        PagedModel<EntityModel<PersonDTO>> result =  service.findByName("First Name Test", PageRequest.of(0, 14));

        List<PersonDTO> people = result.getContent()
                .stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toList());

        assertNotNull(people);
        assertEquals(14, people.size());

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("self")
                        && link.getHref().endsWith("/api/person/v1/1")
                        && link.getType().equals("GET")
                ));

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("findAll")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("GET")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("POST")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("update")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("PUT")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("delete")
                        && link.getHref().endsWith("/api/person/v1/1")
                        && link.getType().equals("DELETE")
                )
        );

        validateIndividualPerson(people.get(1), 1);
        validateIndividualPerson(people.get(4), 4);
        validateIndividualPerson(people.get(7), 7);

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("self")
                        && link.getHref().endsWith("/api/person/v1/1")
                        && link.getType().equals("GET")
                ));

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("findAll")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("GET")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("POST")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("update")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("PUT")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("delete")
                        && link.getHref().endsWith("/api/person/v1/1")
                        && link.getType().equals("DELETE")
                )
        );



    }
    @Test
    void exportPageCSV() throws Exception {

        List<Person> persons = input.mockEntityList();

        Pageable pageable = PageRequest.of(0, 14);

        Page<Person> page = new PageImpl<>(
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

        when(fileExporter.exportPeople(anyList()))
                .thenReturn(resource);

        var result = service.exportPage(
                pageable,
                "text/csv"
        );

        assertNotNull(result);
        assertEquals(resource, result);

        verify(repository).findAll(pageable);
        verify(exporter).getExporter("text/csv");
        verify(fileExporter).exportPeople(anyList());
    }

    @Test
    void exportPageXlsx() throws Exception {

        List<Person> persons = input.mockEntityList();

        Pageable pageable = PageRequest.of(0, 14);

        Page<Person> page = new PageImpl<>(
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

        when(fileExporter.exportPeople(anyList()))
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

        verify(fileExporter).exportPeople(anyList());
    }

    @Test
    void exportPagePdf() throws Exception {

        List<Person> persons = input.mockEntityList();

        Pageable pageable = PageRequest.of(0, 14);

        Page<Person> page = new PageImpl<>(
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

        when(fileExporter.exportPeople(anyList()))
                .thenReturn(resource);

        var result = service.exportPage(
                pageable,
                "application/pdf"
        );

        assertNotNull(result);
        assertEquals(resource, result);

        verify(repository).findAll(pageable);
        verify(exporter).getExporter("application/pdf");
        verify(fileExporter).exportPeople(anyList());
    }

    @Test
    void exportPerson() throws Exception {

        Person person = input.mockEntity(1);

        Resource resource = new ByteArrayResource(
                "test file".getBytes()
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(person));

        when(exporter.getExporter("application/pdf"))
                .thenReturn(fileExporter);

        when(fileExporter.exportPerson(any(PersonDTO.class)))
                .thenReturn(resource);

        var result = service.exportPerson(
                1L,
                "application/pdf"
        );

        assertNotNull(result);
        assertEquals(resource, result);

        verify(repository).findById(1L);
        verify(exporter).getExporter("application/pdf");
        verify(fileExporter).exportPerson(any(PersonDTO.class));
    }
    @Test
    void create() {
        Person person = input.mockEntity(1);
        Person persisted = person;
        persisted.setId(1L);

        PersonDTO dto = input.mockDTO(1);

        when(repository.save(person)).thenReturn(persisted);

        var result = service.create(dto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getLinks());

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("self")
                        && link.getHref().endsWith("/api/person/v1/1")
                        && link.getType().equals("GET")
                ));

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("findAll")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("GET")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("POST")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("update")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("PUT")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("delete")
                        && link.getHref().endsWith("/api/person/v1/1")
                        && link.getType().equals("DELETE")
                )
        );

        assertEquals("Address Test1", result.getAddress());
        assertEquals("First Name Test1", result.getFirstName());
        assertEquals("Last Name Test1", result.getLastName());
        assertEquals("Female", result.getGender());
        assertEquals("Profile Url Test1", person.getProfileUrl());
        assertEquals("Profile Url Test1", person.getProfileUrl());
    }

    @Test
    void massCreationCsv() throws Exception {
        String content = """
            first_name,last_name,address,gender
            Alexandre,Rebechi,Rua A,Male
            João,Silva,Rua B,Female
            """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "people.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );

        List<PersonDTO> people = List.of(
                input.mockDTO(1),
                input.mockDTO(2)
        );

        when(importer.getImporter("people.csv"))
                .thenReturn(fileImporter);

        when(fileImporter.importFile(any(InputStream.class)))
                .thenReturn(people);

        AtomicLong id = new AtomicLong(1);

        when(repository.save(any(Person.class)))
                .thenAnswer(invocation -> {
                    Person person = invocation.getArgument(0);
                    person.setId(id.getAndIncrement());
                    return person;
                });

        var result = service.massCreation(file);

        assertNotNull(result);
        assertEquals(2, result.size());
        var person = result.get(1);

        assertNotNull(person.getLinks());
        person.getLinks().forEach(link ->
                System.out.println(
                        "REL = " + link.getRel().value()
                                + " | HREF = " + link.getHref()
                                + " | TYPE = " + link.getType()
                )
        );

        validateLinks(result.get(0));
        assertEquals("First Name Test1", result.get(0).getFirstName());
        assertEquals("Last Name Test1", result.get(0).getLastName());
        assertEquals("Profile Url Test1", result.get(0).getProfileUrl());

        validateLinks(result.get(1));
        assertEquals("First Name Test2", result.get(1).getFirstName());
        assertEquals("Last Name Test2", result.get(1).getLastName());
        assertEquals("Profile Url Test2", result.get(1).getProfileUrl());

        assertNotNull(result.get(0).getId());
        assertNotNull(result.get(1).getId());

        verify(importer).getImporter("people.csv");
        verify(fileImporter).importFile(any(InputStream.class));
        verify(repository, times(2)).save(any(Person.class));
    }

    @Test
    void massCreationXlsx() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "people.xlsx",
                MediaTypes.APPLICATION_XLSX_VALUE,
                "test file".getBytes()
        );

        List<PersonDTO> people = List.of(
                input.mockDTO(1),
                input.mockDTO(2)
        );

        when(importer.getImporter("people.xlsx"))
                .thenReturn(fileImporter);

        when(fileImporter.importFile(any(InputStream.class)))
                .thenReturn(people);

        AtomicLong id = new AtomicLong(1);

        when(repository.save(any(Person.class)))
                .thenAnswer(invocation -> {
                    Person person = invocation.getArgument(0);
                    person.setId(id.getAndIncrement());
                    return person;
                });

        var result = service.massCreation(file);

        assertNotNull(result);
        assertEquals(2, result.size());
        var person = result.get(1);

        assertNotNull(person.getLinks());

        validateLinks(result.get(0));
        assertEquals("First Name Test1", result.get(0).getFirstName());
        assertEquals("Last Name Test1", result.get(0).getLastName());
        assertEquals("Profile Url Test1", result.get(0).getProfileUrl());

        validateLinks(result.get(1));
        assertEquals("First Name Test2", result.get(1).getFirstName());
        assertEquals("Last Name Test2", result.get(1).getLastName());
        assertEquals("Profile Url Test2", result.get(1).getProfileUrl());

        assertNotNull(result.get(0).getId());
        assertNotNull(result.get(1).getId());

        verify(importer).getImporter("people.xlsx");
        verify(fileImporter).importFile(any(InputStream.class));
        verify(repository, times(2)).save(any(Person.class));
    }

    @Test
    void testCreateWithNullPerson() {
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
        Person person = input.mockEntity(1);
        Person persisted = person;
        persisted.setId(1L);

        PersonDTO dto = input.mockDTO(1);

        when(repository.findById(1L)).thenReturn(Optional.of(person));
        when(repository.save(person)).thenReturn(persisted);

        var result = service.update(dto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getLinks());

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("self")
                        && link.getHref().endsWith("/api/person/v1/1")
                        && link.getType().equals("GET")
                ));

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("findAll")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("GET")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("POST")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("update")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("PUT")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("delete")
                        && link.getHref().endsWith("/api/person/v1/1")
                        && link.getType().equals("DELETE")
                )
        );

        assertEquals("Address Test1", result.getAddress());
        assertEquals("First Name Test1", result.getFirstName());
        assertEquals("Last Name Test1", result.getLastName());
        assertEquals("Female", result.getGender());
        assertEquals("Profile Url Test1", person.getProfileUrl());
        assertEquals("Profile Url Test1", person.getProfileUrl());
    }
    @Test
    void disablePerson() {
        Person person = input.mockEntity(1);
        person.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(person));

        var result = service.disablePerson(1L);


        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getLinks());

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("self")
                        && link.getHref().endsWith("/api/person/v1/1")
                        && link.getType().equals("GET")
                ));

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("findAll")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("GET")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("POST")
                )
        );
        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("POST")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("update")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("PUT")
                )
        );

        assertNotNull(result.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("delete")
                        && link.getHref().endsWith("/api/person/v1/1")
                        && link.getType().equals("DELETE")
                )
        );
    }

    @Test
    void testUpdateWithNullPerson() {
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
        Person person = input.mockEntity(1);
        person.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(person));

        service.delete(1L);
        verify(repository, times(1)).findById(anyLong());
        verify(repository, times(1)).delete(any(Person.class));
        verifyNoMoreInteractions(repository);
    }

    private static void validateIndividualPerson(PersonDTO person, int i) {
        assertNotNull(person);
        assertNotNull(person.getId());
        assertNotNull(person.getLinks());

        assertNotNull(person.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("self")
                        && link.getHref().endsWith("/api/person/v1/" + i)
                        && link.getType().equals("GET")
                ));

        assertNotNull(person.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("findAll")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("GET")
                )
        );

        assertNotNull(person.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("POST")
                )
        );

        assertNotNull(person.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("update")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("PUT")
                )
        );

        assertNotNull(person.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("delete")
                        && link.getHref().endsWith("/api/person/v1/" + i)
                        && link.getType().equals("DELETE")
                )
        );

        assertEquals("Address Test" + i, person.getAddress());
        assertEquals("First Name Test" + i, person.getFirstName());
        assertEquals("Last Name Test" + i, person.getLastName());
        assertEquals(((i % 2)==0) ? "Male" : "Female", person.getGender());
        assertEquals("Profile Url Test" + i, person.getProfileUrl());
        assertEquals("Photo Url Test" + i, person.getPhotoUrl());

    }
    private void validateLinks(PersonDTO person) {

        assertNotNull(person.getLinks());

        assertTrue(person.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("self")
                                && link.getHref().endsWith(
                                "/api/person/v1/" + person.getId()
                        )
                                && link.getType().equals("GET")
                ));

        assertTrue(person.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("findAll")
                                && link.getHref().startsWith(
                                "/api/person/v1"
                        )
                                && "GET".equals(link.getType())
                ));

        assertTrue(person.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("create")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("POST")
                ));

        assertTrue(person.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("massCreation")
                        && link.getHref().endsWith("/api/person/v1/massCreation")
                        && link.getType().equals("POST")
                ));

        assertTrue(person.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("update")
                        && link.getHref().endsWith("/api/person/v1")
                        && link.getType().equals("PUT")
                ));

        assertTrue(person.getLinks().stream()
                .anyMatch(link -> link.getRel().value().equals("delete")
                                && link.getHref().endsWith(
                                "/api/person/v1/" + person.getId()
                        )
                                && link.getType().equals("DELETE")
                ));
    }
}