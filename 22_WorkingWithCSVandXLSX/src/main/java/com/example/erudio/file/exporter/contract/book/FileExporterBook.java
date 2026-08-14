package com.example.erudio.file.exporter.contract.book;

import com.example.erudio.data.dto.BookDTO;
import org.springframework.core.io.Resource;

import java.util.List;

public interface FileExporterBook {

    Resource exportFile(List<BookDTO> books) throws Exception;

}
