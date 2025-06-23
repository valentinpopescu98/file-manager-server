package com.valentin.file_manager_server.service;

import com.valentin.file_manager_server.model.FileMetadata;
import com.valentin.file_manager_server.model.UploadStatus;
import com.valentin.file_manager_server.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileService {

    private final S3Service s3Service;
    private final EmailService emailService;
    private final FileMetadataRepository fileMetadataRepository;
    private final Executor executor;
    private final Map<String, UploadStatus> uploadStatusMap = new ConcurrentHashMap<>();

    public List<FileMetadata> listFiles() {
        try {
            return fileMetadataRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch files: " + e.getMessage(), e);
        }
    }

    private Optional<FileMetadata> findFileByKey(String s3Key) {
        try {
            return fileMetadataRepository.findByS3Key(s3Key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch file: " + e.getMessage(), e);
        }
    }

    public StreamingResponseBody downloadFile(String s3Key, String email) throws FileNotFoundException {
        Optional<FileMetadata> fileOpt = findFileByKey(s3Key);
        if (fileOpt.isEmpty()) {
            throw new FileNotFoundException();
        }
        FileMetadata fileMetadata = fileOpt.get();

        ResponseInputStream<GetObjectResponse> fileStream = s3Service.downloadFile(s3Key);

        return output -> {
            boolean success = false;
            try (fileStream) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fileStream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }

                success = true;
                emailService.sendDownloadNotification(fileMetadata, email);
            } catch (Exception e) {
                log.error("Error during download streaming: {}", e.getMessage());
            } finally {
                if (!success) {
                    log.warn("Download failed or was interrupted for file: {}", s3Key);
                }
            }
        };
    }

    private String generateUniqueUploadId() {
        String uploadId;
        do {
            uploadId = UUID.randomUUID().toString();
        } while (uploadStatusMap.containsKey(uploadId));
        return uploadId;
    }

    public String startUploadFile(MultipartFile file, String name, String description, String uploaderEmail) {
        String uploadId = generateUniqueUploadId();

        uploadStatusMap.put(uploadId, UploadStatus.PROCESSING);
        log.info("File {} with upload ID {} started processing", file, uploadId);

        // Save the file temporary
        File tempFile;
        try {
            tempFile = File.createTempFile("temp_", "_" + file.getOriginalFilename());
            file.transferTo(tempFile);
        } catch (IOException e) {
            uploadStatusMap.put(uploadId, UploadStatus.ERROR);
            throw new RuntimeException("Failed to save temp file: " + e.getMessage(), e);
        }

        // Delegate new thread for upload
        CompletableFuture.runAsync(() ->
                asyncUploadTask(uploadId, tempFile, name, description, uploaderEmail), executor);

        return uploadId;
    }

    private void asyncUploadTask(
            String uploadId, File tempFile, String name, String description, String uploaderEmail) {
        try {
            FileMetadata fileMetadata = uploadFile(tempFile, name, description, uploaderEmail);
            emailService.sendUploadConfirmationEmail(fileMetadata, uploaderEmail);
            uploadStatusMap.put(uploadId, UploadStatus.DONE);
            log.info("File {} with upload ID {} has been uploaded", tempFile.getName(), uploadId);
        } catch (Exception e) {
            uploadStatusMap.put(uploadId, UploadStatus.ERROR);
            log.error("File {} with upload ID {} failed uploaded with error",
                    tempFile.getName(), uploadId, e);
        } finally {
            boolean deleted = tempFile.delete();
            if (!deleted) {
                log.warn("Temp file {} could not be deleted", tempFile.getAbsolutePath());
            }
        }
    }

    private FileMetadata uploadFile(File file, String name, String description, String uploaderEmail) {
        try {
            String s3Key = s3Service.uploadFile(file);
            String url = s3Service.generateFileUrl(s3Key);

            FileMetadata fileMetadata = new FileMetadata(
                    name, description, uploaderEmail, s3Key, url, LocalDateTime.now());
            return fileMetadataRepository.save(fileMetadata);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    public UploadStatus getUploadStatus(String uploadId) {
        return uploadStatusMap.getOrDefault(uploadId, UploadStatus.ERROR);
    }

    public void deleteFile(String s3Key, String email) throws FileNotFoundException {
        Optional<FileMetadata> fileOpt = findFileByKey(s3Key);
        if (fileOpt.isEmpty()) {
            throw new FileNotFoundException();
        }
        FileMetadata fileMetadata = fileOpt.get();

        try {
            s3Service.deleteFile(s3Key);
            fileMetadataRepository.deleteByS3Key(s3Key);

            emailService.sendDeleteNotification(fileMetadata, email);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    public String getOriginalFilename(String s3Key) {
        return s3Service.extractOriginalFilename(s3Key);
    }
}
