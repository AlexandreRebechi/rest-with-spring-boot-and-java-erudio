package com.example.erudio.file.exporter.impl.book;

import com.example.erudio.data.dto.BookDTO;
import com.example.erudio.file.exporter.contract.book.FileExporterBook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Component
public class XlsxExporterBook implements FileExporterBook {
    @Override
    public Resource exportBooks(List<BookDTO> books) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Books");
            CellStyle dateStyle = createDateCellStyle(workbook);

            Row headerRow = sheet.createRow(0);


            String[] headers = {"ID", "Author", "Launch Date", "Price", "Title", "Enabled"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderCellStyle(workbook));
            }

            int rowIndex = 1;
            for (BookDTO book : books) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(book.getId());
                row.createCell(1).setCellValue(book.getAuthor());
                Cell launchDateCell = row.createCell(2);
                launchDateCell.setCellValue(book.getLaunchDate());
                launchDateCell.setCellStyle(dateStyle);
                row.createCell(3).setCellValue(book.getPrice());
                row.createCell(4).setCellValue(book.getTitle());
                row.createCell(5).setCellValue(
                        book.getEnabled() != null && book.getEnabled() ? "Yes" : "Not");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);

            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            return new ByteArrayResource(outputStream.toByteArray());
        }

    }

    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    private CellStyle createDateCellStyle(Workbook workbook) {

        CellStyle style = workbook.createCellStyle();

        CreationHelper creationHelper =
                workbook.getCreationHelper();

        style.setDataFormat(
                creationHelper.createDataFormat()
                        .getFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        );

        return style;
    }
    }

