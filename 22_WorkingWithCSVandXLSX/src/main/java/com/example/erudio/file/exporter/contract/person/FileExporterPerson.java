package com.example.erudio.file.exporter.contract.person;

import com.example.erudio.data.dto.PersonDTO;
import org.springframework.core.io.Resource;

import java.util.List;

public interface FileExporterPerson {

    Resource exportFile(List<PersonDTO> people) throws Exception;

}
