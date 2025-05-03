package com.valentin.demo_aws.controller;

import com.valentin.demo_aws.model.FileMetadata;
import com.valentin.demo_aws.service.EmailService;
import com.valentin.demo_aws.service.FileService;
import com.valentin.demo_aws.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private EmailService emailService;

    @GetMapping
    public ResponseEntity<List<FileMetadata>> listFiles() {
        List<FileMetadata> files = fileService.listFiles();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/download")
    public ResponseEntity<FileMetadata> downloadFile(@RequestParam String key) {
        Optional<FileMetadata> fileOpt = fileService.findFileByKey(key);
        if (fileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        FileMetadata fileMetadata = fileOpt.get();

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        emailService.sendDownloadNotification(fileMetadata, currentUser);
        String url = s3Service.generateFileUrl(fileOpt.get().getKey());

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .body(fileMetadata);
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

            return ResponseEntity.status(HttpStatus.OK)
                    .body("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam String key) {

        try {
            Optional<FileMetadata> fileOpt = fileService.findFileByKey(key);
            if (fileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            FileMetadata fileMetadata = fileOpt.get();

            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            emailService.sendDeleteNotification(fileMetadata, currentUser);
            fileService.deleteFile(key);

            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
