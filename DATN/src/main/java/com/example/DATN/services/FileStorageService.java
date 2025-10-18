
package com.example.DATN.services;

import com.example.DATN.constant.Util.FileUtil;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageFolder = Paths.get("uploads");
    public FileStorageService() {
        try {
            Files.createDirectories(storageFolder);
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize storage folder", e);
        }
    }

    public String storeFile(MultipartFile file, String baseFileName) {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }
        if (file.getSize() >= FileUtil.MAX_FILE_SIZE_MB) {
            throw new ApplicationException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        String fileExtension = ".png";
        String generatedFileName = baseFileName + "-" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;
        Path destinationFilePath = this.storageFolder.resolve(Paths.get(generatedFileName)).normalize().toAbsolutePath();


        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }

        return generatedFileName;
    }

    public void deleteFile(String filename) {
        try {
            Path filePath = this.storageFolder.resolve(filename).normalize().toAbsolutePath();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + filename, e);
        }
    }

    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = this.storageFolder.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + filename, e);
        }
    }

    public String getMediaTypeForFileName(String filename) {
        try {
            Path filePath = this.storageFolder.resolve(filename).normalize();
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                // Fallback to a default if probeContentType returns null
                contentType = "application/octet-stream";
            }
            return contentType;
        } catch (IOException ex) {
            // Log the exception or handle it as appropriate
            return "application/octet-stream"; // Default fallback
        }
    }
}
