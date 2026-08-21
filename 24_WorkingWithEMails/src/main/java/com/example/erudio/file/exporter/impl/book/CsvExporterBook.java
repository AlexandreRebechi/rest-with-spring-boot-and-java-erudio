package com.example.erudio.file.exporter.impl.book;

import com.example.erudio.data.dto.BookDTO;
import com.example.erudio.file.exporter.contract.book.FileExporterBook;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CsvExporterBook implements FileExporterBook {
    @Override
    public Resource exportBooks(List<BookDTO> books) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader("ID", "Author", "Launch Date", "Price", "Title", "Enabled")
                .setSkipHeaderRecord(false)
                .get();

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            for(BookDTO book : books){
                csvPrinter.printRecord(
                        book.getId(),
                        book.getAuthor(),
                        book.getLaunchDate(),
                        book.getPrice(),
                        book.getTitle(),
                        book.getEnabled()
                );
            }
        }
        return new ByteArrayResource(outputStream.toByteArray());
    }
}
