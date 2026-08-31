package com.example.erudio.file.exporter.contract.book;

import com.example.erudio.data.dto.BookDTO;
import org.springframework.core.io.Resource;

import java.util.List;

public interface FileExporterBook {

    Resource exportBooks(List<BookDTO> books) throws Exception;

}
