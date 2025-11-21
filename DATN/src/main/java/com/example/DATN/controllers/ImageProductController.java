
package com.example.DATN.controllers;

import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.services.FileStorageService;
import com.example.DATN.services.ImageProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageProductController {

    private final ImageProductService imageProductService;
    private final FileStorageService fileStorageService;

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteImage(@PathVariable Long id) {
        imageProductService.deleteImage(id);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/view/{filename:.+}")
    public ResponseEntity<Resource> viewImage(@PathVariable String filename) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(filename);
            String contentType = fileStorageService.getMediaTypeForFileName(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (ApplicationException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
