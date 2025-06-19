package com.valentin.file_manager_server.controller;

import com.valentin.file_manager_server.model.FileMetadata;
import com.valentin.file_manager_server.service.EmailService;
import com.valentin.file_manager_server.service.FileService;
import com.valentin.file_manager_server.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping
public class FileController {

    private final FileService fileService;
    private final S3Service s3Service;
    private final EmailService emailService;

    @GetMapping
    public ResponseEntity<List<FileMetadata>> listFiles() {
        List<FileMetadata> files = fileService.listFiles();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam String s3Key) {
        Optional<FileMetadata> fileOpt = fileService.findFileByKey(s3Key);
        if (fileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        FileMetadata fileMetadata = fileOpt.get();

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        emailService.sendDownloadNotification(fileMetadata, currentUser);

        ResponseInputStream<GetObjectResponse> fileStream = fileService.downloadFile(s3Key);
        String originalFilename = fileService.getOriginalFilename(s3Key);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + originalFilename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(fileStream.response().contentLength())
                .body(new InputStreamResource(fileStream));
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
            FileMetadata fileMetadata = fileOpt.get();

            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            emailService.sendDeleteNotification(fileMetadata, currentUser);
            fileService.deleteFile(s3Key);

            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
