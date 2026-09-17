package com.example.erudio.file.importer.impl.person;

import com.example.erudio.data.dto.PersonDTO;
import com.example.erudio.file.importer.contract.person.FileImporterPerson;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvImporterPerson implements FileImporterPerson {

    @Override
    public List<PersonDTO> importFile(InputStream inputStream) throws Exception {
            CSVFormat format = CSVFormat.Builder.create()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .setDelimiter(';')
                    .get();

       Iterable<CSVRecord> records = format.parse(new InputStreamReader(inputStream));



        return parseRecordsToPersonDTOs(records);
    }


    private List<PersonDTO> parseRecordsToPersonDTOs(Iterable<CSVRecord> records) {
        List<PersonDTO> people = new ArrayList<>();

        for (CSVRecord record : records) {
            PersonDTO person = new PersonDTO();
            person.setFirstName(record.get("first_name"));
            person.setLastName(record.get("last_name"));
            person.setAddress(record.get("address"));
            person.setGender(record.get("gender"));
            person.setEnabled(true);
            people.add(person);
        }
        return people;
    }
}
