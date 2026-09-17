package com.example.erudio.file.importer.factory.person;

import com.example.erudio.exception.BadRequestException;
import com.example.erudio.file.importer.contract.person.FileImporterPerson;
import com.example.erudio.file.importer.impl.person.CsvImporterPerson;
import com.example.erudio.file.importer.impl.person.XlsxImporterPerson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class FileImporterFactoryPerson {

    private Logger looger  = LoggerFactory.getLogger(FileImporterFactoryPerson.class);

    @Autowired
    private ApplicationContext context;

    public FileImporterPerson getImporter(String fileName) throws Exception {
        if(fileName.endsWith(".xlsx")){
            return context.getBean(XlsxImporterPerson.class);
            //return new XlsxImporter();

        }else  if(fileName.endsWith(".csv")){
            return context.getBean(CsvImporterPerson.class);
            //return new CsvImporter();
        }else {
            throw new BadRequestException("Invalid File Format!");
        }
    }




}
