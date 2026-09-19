package com.example.erudio.file.exporter.impl.person;

import com.example.erudio.data.dto.PersonDTO;
import com.example.erudio.file.exporter.contract.person.FileExporterPerson;
import com.example.erudio.services.QRCodeService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
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
        // Carrega o .jasper pré-compilado diretamente
        InputStream jasperStream = getClass().getResourceAsStream("/templates/people.jasper");
        if (jasperStream == null) {
            throw new RuntimeException("Compiled report file not found: /templates/people.jasper");
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(people);
        Map<String, Object> parameters = new HashMap<>();

        // Preenche diretamente sem chamar JasperCompileManager
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperStream, parameters, dataSource);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        }
    }

    @Override
    public Resource exportPerson(PersonDTO person) throws Exception {
        InputStream mainJasperStream = getClass().getResourceAsStream("/templates/person.jasper");
        if (mainJasperStream == null) {
            throw new RuntimeException("Compiled report file not found: /templates/person.jasper");
        }

        InputStream subReportStream = getClass().getResourceAsStream("/templates/bookSub.jasper");
        if (subReportStream == null) {
            throw new RuntimeException("Compiled report file not found: /templates/bookSub.jasper");
        }

        // Carrega o subrelatório em memória
        JasperReport subReport = (JasperReport) JRLoader.loadObject(subReportStream);

        InputStream qrCodeStream = service.generateQRCode(person.getProfileUrl(), 200, 200);
        JRBeanCollectionDataSource mainDataSource = new JRBeanCollectionDataSource(Collections.singletonList(person));
        JRBeanCollectionDataSource subReportDataSource = new JRBeanCollectionDataSource(person.getBooks());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("SUB_REPORT_DATA_SOURCE", subReportDataSource);
        parameters.put("BOOK_SUB_REPORT", subReport);
        // REMOVIDO: parameters.put("SUB_REPORT_DIR", path); -> Não é necessário ao passar o objeto "subReport" diretamente
        parameters.put("QR_CODEIMAGE", qrCodeStream);

        JasperPrint jasperPrint = JasperFillManager.fillReport(mainJasperStream, parameters, mainDataSource);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        }
    }
}