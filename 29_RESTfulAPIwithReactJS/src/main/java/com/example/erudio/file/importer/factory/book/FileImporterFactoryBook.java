package com.example.erudio.file.importer.factory.book;

import com.example.erudio.exception.BadRequestException;
import com.example.erudio.file.importer.contract.book.FileImporterBook;
import com.example.erudio.file.importer.impl.book.CsvImporterBook;
import com.example.erudio.file.importer.impl.book.XlsxImporterBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class FileImporterFactoryBook {

    private Logger looger  = LoggerFactory.getLogger(FileImporterFactoryBook.class);

    @Autowired
    private ApplicationContext context;

    public FileImporterBook getImporter(String fileName) throws Exception {
        if(fileName.endsWith(".xlsx")){
            return context.getBean(XlsxImporterBook.class);
            //return new XlsxImporter();

        }else  if(fileName.endsWith(".csv")){
            return context.getBean(CsvImporterBook.class);
            //return new CsvImporter();
        }else {
            throw new BadRequestException("Invalid File Format!");
        }
    }




}
