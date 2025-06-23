package com.valentin.file_manager_server.controller;

import com.valentin.file_manager_server.model.FileMetadata;
import com.valentin.file_manager_server.model.UploadStatus;
import com.valentin.file_manager_server.model.UploadStatusResponse;
import com.valentin.file_manager_server.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class FileController {

    private final FileService fileService;

    @GetMapping
    public ResponseEntity<List<FileMetadata>> listFiles() {
        try {
            List<FileMetadata> files = fileService.listFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            throw new RuntimeException("Cannot fetch file metadata: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadFile(@RequestParam String s3Key) {
        try {
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            StreamingResponseBody body = fileService.downloadFile(s3Key, currentUser);

            String originalFilename = fileService.getOriginalFilename(s3Key);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + originalFilename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(body);
        } catch (Exception e) {
            throw new RuntimeException("File download failed: " + e.getMessage());
        }
    }

    @GetMapping("/upload/status")
    public ResponseEntity<UploadStatusResponse> getUploadStatus(@RequestParam String uploadId) {
        try {
            UploadStatus status = fileService.getUploadStatus(uploadId);
            return ResponseEntity.ok(new UploadStatusResponse(status));
        } catch (Exception e) {
            throw new RuntimeException("Cannot fetch file upload status: " + e.getMessage());
        }
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
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam String s3Key) {
        try {
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            fileService.deleteFile(s3Key, currentUser);

            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            throw new RuntimeException("File deletion failed: " + e.getMessage());
        }
    }
}
