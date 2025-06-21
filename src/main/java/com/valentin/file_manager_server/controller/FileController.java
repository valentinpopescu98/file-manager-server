package com.valentin.file_manager_server.controller;

import com.valentin.file_manager_server.model.FileMetadata;
import com.valentin.file_manager_server.model.UploadStatus;
import com.valentin.file_manager_server.model.UploadStatusResponse;
import com.valentin.file_manager_server.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileNotFoundException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class FileController {

    private final FileService fileService;

    @GetMapping
    public ResponseEntity<?> listFiles() {

        try {
            List<FileMetadata> files = fileService.listFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File metadata fetch failed: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam String s3Key) {

        try {
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            StreamingResponseBody body = fileService.downloadFile(s3Key, currentUser);

            String originalFilename = fileService.getOriginalFilename(s3Key);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + originalFilename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(body);
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("File not found");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File download failed: " + e.getMessage());
        }
    }

    @GetMapping("/upload/status")
    public ResponseEntity<UploadStatusResponse> getUploadStatus(@RequestParam String uploadId) {
        UploadStatus status = fileService.getUploadStatus(uploadId);
        return ResponseEntity.ok(new UploadStatusResponse(status));
    }

    @PostMapping("/upload")
    public ResponseEntity<String> startUploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description) {

        try {
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            String uploadId = fileService.startUploadFile(file, file.getOriginalFilename(), description, currentUser);

            return ResponseEntity.accepted().body(uploadId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam String s3Key) {

        try {
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            fileService.deleteFile(s3Key, currentUser);

            return ResponseEntity.ok("File deleted successfully");
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("File not found");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
