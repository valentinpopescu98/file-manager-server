package com.valentin.file_manager_server.service;

import com.valentin.file_manager_server.model.FileMetadata;
import com.valentin.file_manager_server.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FileService {

    private final S3Service s3Service;
    private final FileMetadataRepository fileMetadataRepository;

    public List<FileMetadata> listFiles() {
        return fileMetadataRepository.findAll();
    }

    public Optional<FileMetadata> findFileByKey(String key) {
        return fileMetadataRepository.findByKey(key);
    }

    public ResponseInputStream<GetObjectResponse> downloadFile(String key) {
        return s3Service.downloadFile(key);
    }

    public FileMetadata uploadFile(MultipartFile file, String name, String description, String uploaderEmail)
            throws IOException {
        String key = s3Service.uploadFile(file);
        String url = s3Service.generateFileUrl(key);

        FileMetadata fileMetadata = new FileMetadata(name, description, uploaderEmail, key, url, LocalDateTime.now());
        return fileMetadataRepository.save(fileMetadata);
    }

    public void deleteFile(String key) {
        s3Service.deleteFile(key);
        fileMetadataRepository.deleteByKey(key);
    }

    public String getOriginalFilename(String key) {
        return s3Service.extractOriginalFilename(key);
    }
}
