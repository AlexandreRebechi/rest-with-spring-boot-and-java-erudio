package com.example.erudio.file.importer.contract.person;

import com.example.erudio.data.dto.PersonDTO;

import java.io.InputStream;
import java.util.List;

public interface FileImporterPerson {

    List<PersonDTO> importFile(InputStream inputStream) throws Exception;

}
