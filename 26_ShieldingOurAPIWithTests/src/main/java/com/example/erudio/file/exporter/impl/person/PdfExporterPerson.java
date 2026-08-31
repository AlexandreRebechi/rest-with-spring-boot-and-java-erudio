package com.example.erudio.file.exporter.impl.person;

import com.example.erudio.data.dto.PersonDTO;
import com.example.erudio.file.exporter.contract.person.FileExporterPerson;
import com.example.erudio.services.QRCodeService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PdfExporterPerson implements FileExporterPerson {

    @Autowired
    private QRCodeService service;

    @Override
    public Resource exportPeople(List<PersonDTO> people) throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/templates/people.jrxml");
        if(inputStream == null) {
            throw new RuntimeException("Template file not found /templates/people.jrxml");
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(people);
        Map<String, Object> parameters = new HashMap<String, Object>();
        //parameters.put("title", "People Report");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,parameters, dataSource);

       try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
           JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
           return new ByteArrayResource(outputStream.toByteArray());
       }
    }

    @Override
    public Resource exportPerson(PersonDTO person) throws Exception {
        InputStream mainTemplateStream = getClass().getResourceAsStream("/templates/person.jrxml");
        if(mainTemplateStream == null) {
            throw new RuntimeException("Template file not found /templates/person.jrxml");
        }
        InputStream subReportStream = getClass().getResourceAsStream("/templates/bookSub.jrxml");
        if(subReportStream == null) {
            throw new RuntimeException("Template file not found /templates/bookSub.jrxml");
        }

        JasperReport mainReport = JasperCompileManager.compileReport(mainTemplateStream);
        JasperReport subReport = JasperCompileManager.compileReport(subReportStream);

        String path = getClass().getResource("/templates/bookSub.jrxml").getPath();

        InputStream qrCodeStream = service.generateQRCode(person.getProfileUrl(), 200, 200);
        JRBeanCollectionDataSource mainDataSource = new JRBeanCollectionDataSource(Collections.singletonList(person));
        JRBeanCollectionDataSource subReportDataSource = new JRBeanCollectionDataSource(person.getBooks());
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("SUB_REPORT_DATA_SOURCE", subReportDataSource);
        parameters.put("BOOK_SUB_REPORT", subReport);
        parameters.put("SUB_REPORT_DIR", path);
        parameters.put("QR_CODEIMAGE", qrCodeStream);




        JasperPrint jasperPrint = JasperFillManager.fillReport(mainReport,parameters, mainDataSource);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        }
    }
}


