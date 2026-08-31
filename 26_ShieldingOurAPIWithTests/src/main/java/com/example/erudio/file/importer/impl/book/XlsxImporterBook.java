package com.example.erudio.file.importer.impl.book;

import com.example.erudio.data.dto.BookDTO;
import com.example.erudio.file.importer.contract.book.FileImporterBook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class XlsxImporterBook implements FileImporterBook {
    @Override
    public List<BookDTO> importFile(InputStream inputStream) throws Exception {

        try(XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            // first spreadsheet
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if(rowIterator.hasNext()) rowIterator.next();

            return parseRowsToBooksDtoList(rowIterator);
        }
    }

    private List<BookDTO> parseRowsToBooksDtoList(Iterator<Row> rowIterator) {
        List<BookDTO> people = new ArrayList<>();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if(isRowValid(row)) {
                people.add(parseRowToBooksDto(row));

            };
        }

        return people;



    }

    private BookDTO parseRowToBooksDto(Row row) {
        BookDTO book = new BookDTO();
        book.setAuthor(row.getCell(0).getStringCellValue());
        try {
            SimpleDateFormat dateFormat =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

            book.setLaunchDate(
                    dateFormat.parse(
                            row.getCell(1).getStringCellValue()
                    )
            );

        } catch (ParseException e) {
            throw new RuntimeException(
                    "Erro ao converter launchDate: "
                            + row.getCell(1).getStringCellValue(),
                    e
            );
        }
        book.setPrice(row.getCell(2).getNumericCellValue());
        book.setTitle(row.getCell(3).getStringCellValue());
        book.setEnabled(true);
        return book;

    }

    private static boolean isRowValid(Row row) {
        return row.getCell(0) != null && row.getCell(0).getCellType() != CellType.BLANK;
    }
}
