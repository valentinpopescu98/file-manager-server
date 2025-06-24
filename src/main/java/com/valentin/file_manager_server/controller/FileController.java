package com.valentin.file_manager_server.controller;

import com.valentin.file_manager_server.model.FileMetadata;
import com.valentin.file_manager_server.model.UploadStatus;
import com.valentin.file_manager_server.model.UploadStatusResponse;
import com.valentin.file_manager_server.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class FileController {

    private final FileService fileService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listFiles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("List files requested by user '{}'", currentUser);

        try {
            Page<FileMetadata> filePage = fileService.listFiles(PageRequest.of(page - 1, limit));
            Map<String, Object> response = new HashMap<>();
            response.put("files", filePage.getContent());
            response.put("total", filePage.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Cannot fetch file metadata: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadFile(@RequestParam String s3Key) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Download requested by user '{}' for file '{}'", currentUser, s3Key);

        try {
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

    @PostMapping("/upload")
    public ResponseEntity<String> startUploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description) {

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Upload requested by user '{}' for file '{}'", currentUser, file.getOriginalFilename());

        try {
            String uploadId = fileService.startUploadFile(file, file.getOriginalFilename(), description, currentUser);
            return ResponseEntity.accepted().body(uploadId);
        } catch (Exception e) {
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/upload/status")
    public ResponseEntity<UploadStatusResponse> getUploadStatus(@RequestParam String uploadId) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        UploadStatus status = fileService.getUploadStatus(uploadId);
        log.info("Upload status requested by user '{}' for upload id '{}'", currentUser, uploadId);

        return ResponseEntity.ok(new UploadStatusResponse(status));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam String s3Key) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Upload requested by user '{}' for file '{}'", currentUser, s3Key);

        try {
            fileService.deleteFile(s3Key, currentUser);
            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            throw new RuntimeException("File deletion failed: " + e.getMessage());
        }
    }
}
