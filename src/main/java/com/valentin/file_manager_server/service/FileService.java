package com.valentin.file_manager_server.service;

import com.valentin.file_manager_server.model.FileMetadata;
import com.valentin.file_manager_server.model.dto.UploadResponse;
import com.valentin.file_manager_server.model.dto.UploadResult;
import com.valentin.file_manager_server.model.dto.UploadTaskContext;
import com.valentin.file_manager_server.model.enums.FileAction;
import com.valentin.file_manager_server.model.enums.UploadStatus;
import com.valentin.file_manager_server.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileService {

    private final S3Service s3Service;
    private final EmailService emailService;
    private final FilesActionLogService filesActionLogService;
    private final FileMetadataRepository fileMetadataRepository;
    private final Map<String, UploadStatus> uploadStatusMap = new ConcurrentHashMap<>();
    private final Executor executor = Executors.newFixedThreadPool(4);;

    public Page<FileMetadata> listFiles(Pageable pageable, Specification<FileMetadata> spec) {
        try {
            return fileMetadataRepository.findAll(spec, pageable);
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
                filesActionLogService.logAction(FileAction.DOWNLOAD, email, fileMetadata.getId());
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

    public List<UploadResponse> startUploadFiles(List<MultipartFile> files, String description, String uploaderEmail) {
        List<UploadTaskContext> tasksContext = new ArrayList<>();
        for (MultipartFile file : files) {
            String uploadId = generateUniqueUploadId();

            uploadStatusMap.put(uploadId, UploadStatus.PROCESSING);
            log.info("File {} (size={} bytes) with upload ID {} started processing",
                    file.getOriginalFilename(), file.getSize(), uploadId);

            // Save the file temporary
            File temp;
            try {
                temp = File.createTempFile("temp_", "_" + file.getOriginalFilename());
                file.transferTo(temp);
                tasksContext.add(new UploadTaskContext(uploadId, file.getOriginalFilename(), temp));
            } catch (IOException e) {
                uploadStatusMap.put(uploadId, UploadStatus.ERROR);
                tasksContext.add(new UploadTaskContext(uploadId, file.getOriginalFilename(), null));
            }
        }

        // Delegate new thread for upload
        boolean asyncScheduled = false;
        try {
            CompletableFuture.runAsync(() ->
                    asyncUploadTask(tasksContext, description, uploaderEmail), executor);
            asyncScheduled = true;
        } catch (Exception e) {
            log.error("Failed to start new thread for upload task", e);
        } finally {
            if (!asyncScheduled) {
                for (UploadTaskContext context : tasksContext) {
                    uploadStatusMap.put(context.getUploadId(), UploadStatus.ERROR);

                    File file = context.getContent();
                    if (file != null) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            log.warn("Temp file {} could not be deleted", file.getAbsolutePath());
                        }
                    }
                }
            }
        }

        return tasksContext.stream()
                .map(this::taskContextToRes)
                .toList();
    }

    private void asyncUploadTask(List<UploadTaskContext> tasksContext, String description, String uploaderEmail) {
        // Delegate 4 parallel async uploads
        List<CompletableFuture<UploadResult>> futures = tasksContext.stream()
                .map(context -> CompletableFuture.supplyAsync(() -> {
                    File file = context.getContent();
                    String safeName = file != null ? file.getName() : context.getOriginalName();
                    FileMetadata fileMetadata = null;
                    UploadStatus finalStatus;

                    try {
                        if (context.getContent() == null) {
                            log.warn("Skipping file {} with upload ID {}: no content",
                                    context.getOriginalName(), context.getUploadId());
                            uploadStatusMap.put(context.getUploadId(), UploadStatus.ERROR);
                            return null;
                        }

                        fileMetadata = uploadFile(context.getContent(), context.getOriginalName(), description, uploaderEmail);

                        filesActionLogService.logAction(FileAction.UPLOAD, uploaderEmail, fileMetadata.getId());
                        uploadStatusMap.put(context.getUploadId(), UploadStatus.DONE);
                        finalStatus = UploadStatus.DONE;

                        log.info("File {} with upload ID {} has been uploaded",
                                safeName, context.getUploadId());
                    } catch (Exception e) {
                        uploadStatusMap.put(context.getUploadId(), UploadStatus.ERROR);
                        finalStatus = UploadStatus.ERROR;

                        log.error("File {} with upload ID {} failed uploaded with error",
                                safeName, context.getUploadId(), e);
                    } finally {
                        if (file != null) {
                            boolean deleted = file.delete();
                            if (!deleted) {
                                log.warn("Temp file {} could not be deleted", file.getAbsolutePath());
                            }
                        }
                    }

                    return new UploadResult(
                            context.getOriginalName(),
                            description,
                            fileMetadata == null ? null : fileMetadata.getUploadedAt(),
                            finalStatus
                    );
                }, executor))
                .toList();

        // Wait for all uploads to finish
        List<UploadResult> results = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull) // exclude uploads with no content
                .toList();

        emailService.sendUploadConfirmationEmail(results, uploaderEmail);
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

            filesActionLogService.logAction(FileAction.DELETE, email, fileMetadata.getId());
            emailService.sendDeleteNotification(fileMetadata, email);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    public String getOriginalFilename(String s3Key) {
        return s3Service.extractOriginalFilename(s3Key);
    }

    private UploadResponse taskContextToRes(UploadTaskContext taskContext) {
        return new UploadResponse(taskContext.getUploadId(), taskContext.getOriginalName());
    }
}
