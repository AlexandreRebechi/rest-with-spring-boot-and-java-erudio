package com.example.erudio.file.importer.impl.person;

import com.example.erudio.data.dto.PersonDTO;
import com.example.erudio.file.importer.contract.person.FileImporterPerson;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class XlsxImporterPerson implements FileImporterPerson {
    @Override
    public List<PersonDTO> importFile(InputStream inputStream) throws Exception {

        try(XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            // first spreadsheet
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if(rowIterator.hasNext()) rowIterator.next();

            return parseRowsToPersonDtoList(rowIterator);
        }
    }

    private List<PersonDTO> parseRowsToPersonDtoList(Iterator<Row> rowIterator) {
        List<PersonDTO> people = new ArrayList<>();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if(isRowValid(row)) {
                people.add(parseRowToPersonDto(row));

            };
        }

        return people;



    }

    private PersonDTO parseRowToPersonDto(Row row) {
        PersonDTO person = new PersonDTO();
        person.setFirstName(row.getCell(0).getStringCellValue());
        person.setLastName(row.getCell(1).getStringCellValue());
        person.setAddress(row.getCell(2).getStringCellValue());
        person.setGender(row.getCell(3).getStringCellValue());
        person.setEnabled(true);
        return person;

    }

    private static boolean isRowValid(Row row) {
        return row.getCell(0) != null && row.getCell(0).getCellType() != CellType.BLANK;
    }
}
