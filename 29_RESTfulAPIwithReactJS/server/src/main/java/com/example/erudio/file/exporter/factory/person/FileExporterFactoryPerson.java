package com.example.erudio.file.exporter.factory.person;

import com.example.erudio.exception.BadRequestException;
import com.example.erudio.file.exporter.MediaTypes;
import com.example.erudio.file.exporter.contract.person.FileExporterPerson;
import com.example.erudio.file.exporter.impl.person.CsvExporterPerson;
import com.example.erudio.file.exporter.impl.person.PdfExporterPerson;
import com.example.erudio.file.exporter.impl.person.XlsxExporterPerson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class FileExporterFactoryPerson {

    private Logger looger  = LoggerFactory.getLogger(FileExporterFactoryPerson.class);

    @Autowired
    private ApplicationContext context;

    public FileExporterPerson getExporter(String acceptHeader) throws Exception {
        if(acceptHeader.equalsIgnoreCase(MediaTypes.APPLICATION_XLSX_VALUE)) {
            return context.getBean(XlsxExporterPerson.class);
            //return new XlsxImporter();

        }else  if(acceptHeader.equalsIgnoreCase(MediaTypes.APPLICATION_CSV_VALUE)){
            return context.getBean(CsvExporterPerson.class);
            //return new CsvImporter();
        }else  if(acceptHeader.equalsIgnoreCase(MediaTypes.APPLICATION_PDF_VALUE)){
            return context.getBean(PdfExporterPerson.class);
            //return new CsvImporter();
        }else {
            throw new BadRequestException("Invalid File Format!");
        }
    }




}
