package com.valentin.file_manager_server.controller;

import com.valentin.file_manager_server.model.FileMetadata;
import com.valentin.file_manager_server.service.EmailService;
import com.valentin.file_manager_server.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class FileController {

    private final FileService fileService;
    private final EmailService emailService;

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
            Optional<FileMetadata> fileOpt = fileService.findFileByKey(s3Key);
            if (fileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            FileMetadata fileMetadata = fileOpt.get();
            ResponseInputStream<GetObjectResponse> fileStream = fileService.downloadFile(s3Key);

            emailService.sendDownloadNotification(fileMetadata, currentUser);

            String originalFilename = fileService.getOriginalFilename(s3Key);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + originalFilename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileStream.response().contentLength())
                    .body(new InputStreamResource(fileStream));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File download failed: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description) {

        try {
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            FileMetadata fileMetadata = fileService.uploadFile(
                    file, file.getOriginalFilename(), description, currentUser);

            emailService.sendUploadConfirmationEmail(fileMetadata, currentUser);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam String s3Key) {

        try {
            Optional<FileMetadata> fileOpt = fileService.findFileByKey(s3Key);
            if (fileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            FileMetadata fileMetadata = fileOpt.get();
            fileService.deleteFile(s3Key);

            emailService.sendDeleteNotification(fileMetadata, currentUser);

            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
