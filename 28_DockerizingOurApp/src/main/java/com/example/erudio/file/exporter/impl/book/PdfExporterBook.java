package com.example.erudio.file.exporter.impl.book;

import com.example.erudio.data.dto.BookDTO;
import com.example.erudio.file.exporter.contract.book.FileExporterBook;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PdfExporterBook implements FileExporterBook {

    @Override
    public Resource exportBooks(List<BookDTO> books) throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/templates/books.jrxml");
        if(inputStream == null) {
            throw new RuntimeException("Template file not found /templates/books.jrxml");
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(books);
        Map<String, Object> parameters = new HashMap<String, Object>();
        //parameters.put("title", "Books Report");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,parameters, dataSource);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        }
    }
}

