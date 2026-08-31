package com.example.erudio.file.importer.contract.book;

import com.example.erudio.data.dto.BookDTO;

import java.io.InputStream;
import java.util.List;

public interface FileImporterBook {

    List<BookDTO> importFile(InputStream inputStream) throws Exception;

}
