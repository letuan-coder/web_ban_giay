
package com.example.DATN.services;

import com.example.DATN.constant.Util.FileUtil;
import com.example.DATN.dtos.request.StoreFileRequest;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

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
    private final Path storageBannerFolder = Paths.get("uploads/banners");
    private final Path storageThumbnailFolder = Paths.get("uploads/thumbnails");
    private final Path storeDefaultFolder = Paths.get("/uploads/default/");
    private final Path storeAvatarFolder = Paths.get("uploads/avatar/");
    private final Path storeReturnOrderFodler = Paths.get("uploads/order/");

    public FileStorageService() {
        try {
            Files.createDirectories(storageFolder);
            Files.createDirectories(storageBannerFolder);
            Files.createDirectories(storageThumbnailFolder);
            Files.createDirectories(storeDefaultFolder);
            Files.createDirectories(storeAvatarFolder);
            Files.createDirectories(storeReturnOrderFodler);


        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize storage folder", e);
        }
    }

    public String storeFile(
            StoreFileRequest request) {
        if (request.getFile().isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }
        if (request.getFile().getSize() >= FileUtil.MAX_FILE_SIZE_MB) {
            throw new ApplicationException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        String fileExtension = ".png";
        String generatedFileName = request.getFileName() + "-"
                + UUID.randomUUID().toString().substring(0, 8) + fileExtension;
        Path destinationFilePath = this.storeDefaultFolder.
                resolve(Paths.get(generatedFileName)).normalize().toAbsolutePath();
        if (request.getBanner() != null) {
            destinationFilePath = this.storeDefaultFolder.
                    resolve(Paths.get(generatedFileName)).normalize().toAbsolutePath();
        } else if (request.getProduct() != null) {
            destinationFilePath = this.storageThumbnailFolder.
                    resolve(Paths.get(generatedFileName)).normalize().toAbsolutePath();
        }
        else if (request.getUserAvatar()!=null){
            destinationFilePath = this.storeAvatarFolder.
                    resolve(Paths.get(generatedFileName)).normalize().toAbsolutePath();
        }
        else if (request.getImageOrderReturn()!=null){
            destinationFilePath = this.storeAvatarFolder.
                    resolve(Paths.get(generatedFileName)).normalize().toAbsolutePath();
        }
        else {
            destinationFilePath = this.storageBannerFolder.
                    resolve(Paths.get(generatedFileName)).normalize().toAbsolutePath();
        }
        try (InputStream inputStream = request.getFile().getInputStream()) {
            Files.copy(inputStream, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
        return generatedFileName;
    }


    public void deleteFile(String filename) {
        try {
            Path filePath = findFileInAllFolders(filename);
            if (filePath != null) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + filename, e);
        }
    }

    private Path findFileInAllFolders(String filename) {
        Path[] folders = {
                storageFolder,
                storageBannerFolder,
                storageThumbnailFolder,
                storeAvatarFolder,
                storeDefaultFolder
        };

        for (Path folder : folders) {
            Path filePath = folder.resolve(filename).normalize();
            if (Files.exists(filePath)) {
                return filePath;
            }

        }
        return null;
    }

    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = findFileInAllFolders(filename);
            if (filePath == null) {
                return loadDefaultImage();

            }
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

    private Resource loadDefaultImage() {
        try {
            Path defaultPath = Paths.get("uploads/default/default.png").normalize();
            Resource resource = new UrlResource(defaultPath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Default image not found");
            }

        } catch (Exception ex) {
            throw new RuntimeException("Cannot load default image", ex);
        }
    }

    public String getMediaTypeForFileName(String filename) {
        try {
            Path filePath = findFileInAllFolders(filename);
            if (filePath == null) {
                return "application/octet-stream";
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            return contentType;

        } catch (Exception e) {
            return "application/octet-stream";
        }
    }
}
