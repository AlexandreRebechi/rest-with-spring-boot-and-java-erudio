package com.example.erudio.file.exporter.factory.book;

import com.example.erudio.exception.BadRequestException;
import com.example.erudio.file.exporter.MediaTypes;
import com.example.erudio.file.exporter.contract.book.FileExporterBook;
import com.example.erudio.file.exporter.impl.book.CsvExporterBook;
import com.example.erudio.file.exporter.impl.book.PdfExporterBook;
import com.example.erudio.file.exporter.impl.book.XlsxExporterBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class FileExporterFactoryBook {

    private Logger looger  = LoggerFactory.getLogger(FileExporterFactoryBook.class);

    @Autowired
    private ApplicationContext context;

    public FileExporterBook getExporter(String acceptHeader) throws Exception {
        if(acceptHeader.equalsIgnoreCase(MediaTypes.APPLICATION_XLSX_VALUE)){
            return context.getBean(XlsxExporterBook.class);
            //return new XlsxImporter();

        }else  if(acceptHeader.equalsIgnoreCase(MediaTypes.APPLICATION_CSV_VALUE)){
            return context.getBean(CsvExporterBook.class);
            //return new CsvImporter();
        }else  if(acceptHeader.equalsIgnoreCase(MediaTypes.APPLICATION_PDF_VALUE)){
            return context.getBean(PdfExporterBook.class);
            //return new CsvImporter();
        }else {
            throw new BadRequestException("Invalid File Format!");
        }
    }




}
