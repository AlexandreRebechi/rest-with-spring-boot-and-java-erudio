package com.example.erudio.services;

import com.example.erudio.config.FileStorageConfig;
import com.example.erudio.exception.FileNotFoundException;
import com.example.erudio.exception.FileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private static Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageConfig fileStorageConfig) {
        Path path = Path.of(fileStorageConfig.getUploadDir()).toAbsolutePath()
                .toAbsolutePath().normalize();

        this.fileStorageLocation = path;
        try {
            logger.info("Creating Directory");
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception e){
            logger.error("Could not create the directory where files will be stored!");
            throw new FileStorageException("Could not create the directory where files will be stored!", e);
        }
    }

    public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // ../
            if(fileName.contains("..")){
                logger.error("Sorry FileName contains invalid path sequence {}", fileName);
                throw new FileStorageException("Sorry FileName contains invalid path sequence " + fileName);

            }

            logger.info("Saving file in Disk");
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        }catch (Exception e){
            logger.error("Could not store file {} Please try Again!", fileName);
            throw new FileStorageException("Could not store file " + fileName + "Please try Again!", e);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try{
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            }else {
                logger.error("File not found {}", fileName);
                throw new FileNotFoundException("File not found " + fileName);
            }
        }catch (Exception e){
            logger.error("File not found {}", fileName);
            throw new FileNotFoundException("File not found " + fileName, e);
        }
    }
}
