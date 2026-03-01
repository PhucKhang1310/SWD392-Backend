package swd392.backend.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd392.backend.domain.service.storage.StorageService;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "products") String folder) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            // Upload file
            String objectName = storageService.uploadFile(file, folder);

            // Generate URL
            String fileUrl = storageService.getFileUrl(objectName, "");

            Map<String, String> response = new HashMap<>();
            response.put("fileName", objectName);
            response.put("fileUrl", fileUrl);
            response.put("message", "File uploaded successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }

    @GetMapping("/url")
    public ResponseEntity<Map<String, String>> getFileUrl(
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "folder", defaultValue = "") String folder) {
        try {
            String fileUrl = storageService.getFileUrl(fileName, folder);
            if (fileUrl == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(Map.of("fileUrl", fileUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to get file URL: " + e.getMessage()));
        }
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "folder", defaultValue = "") String folder) {
        try {
            InputStream fileStream = storageService.getFile(fileName, folder);

            // Extract just the filename without folder path
            String downloadFilename = fileName.contains("/")
                    ? fileName.substring(fileName.lastIndexOf("/") + 1)
                    : fileName;

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFilename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(fileStream));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteFile(
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "folder", defaultValue = "") String folder) {
        try {
            storageService.deleteFile(fileName, folder);
            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to delete file: " + e.getMessage()));
        }
    }
}
