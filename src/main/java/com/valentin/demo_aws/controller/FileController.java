package com.valentin.demo_aws.controller;

import com.valentin.demo_aws.model.FileMetadata;
import com.valentin.demo_aws.service.FileService;
import com.valentin.demo_aws.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<List<String>> listFiles() {
        List<String> keys = fileService.listAllKeys();
        return ResponseEntity.ok(keys);
    }

    @GetMapping("/download")
    public ResponseEntity<Void> downloadFile(@RequestParam String key) {
        Optional<FileMetadata> fileOpt = fileService.findFileByKey(key);
        if (fileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String url = s3Service.generateFileUrl(fileOpt.get().getKey());
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description) {

        try {
            fileService.uploadFile(file, file.getOriginalFilename(), description);
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
            fileService.deleteFile(key);
            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
