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
        // Carrega diretamente o .jasper pré-compilado pelo Jaspersoft Studio
        InputStream jasperStream = getClass().getResourceAsStream("/templates/books.jasper");
        if (jasperStream == null) {
            throw new RuntimeException("Compiled report file not found: /templates/books.jasper");
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(books);
        Map<String, Object> parameters = new HashMap<>();

        // Preenche o relatório sem passar por compilação em tempo de execução
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperStream, parameters, dataSource);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        }
    }
}