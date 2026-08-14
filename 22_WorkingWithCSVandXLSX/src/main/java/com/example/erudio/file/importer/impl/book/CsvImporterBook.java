package com.example.erudio.file.importer.impl.book;

import com.example.erudio.data.dto.BookDTO;
import com.example.erudio.file.importer.contract.book.FileImporterBook;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvImporterBook implements FileImporterBook {

    @Override
    public List<BookDTO> importFile(InputStream inputStream) throws Exception {
            CSVFormat format = CSVFormat.Builder.create()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .setDelimiter(';')
                    .get();

       Iterable<CSVRecord> records = format.parse(new InputStreamReader(inputStream));



        return parseRecordsToBookDTOs(records);
    }


    private List<BookDTO> parseRecordsToBookDTOs(Iterable<CSVRecord> records) {
        List<BookDTO> books = new ArrayList<>();

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

        for (CSVRecord record : records) {
            BookDTO book = new BookDTO();

            book.setAuthor(record.get("author"));

            try {
                book.setLaunchDate(dateFormat.parse(record.get("launch_date")));
            }catch (ParseException e){
                throw new RuntimeException(
                        "Erro ao converter LaunchDate: "+ record.get("launch_date"), e
                );
            }


            book.setPrice(
                    Double.parseDouble(record.get("price"))
            );

            book.setTitle(record.get("title"));
            book.setEnabled(true);

            books.add(book);
        }
        return books;
    }
}
