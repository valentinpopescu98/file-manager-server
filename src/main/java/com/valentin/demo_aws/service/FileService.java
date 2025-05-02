package com.valentin.demo_aws.service;

import com.valentin.demo_aws.model.FileMetadata;
import com.valentin.demo_aws.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class FileService {

    @Autowired
    private S3Service s3Service;
    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    public FileMetadata uploadFile(MultipartFile file, String name, String description) throws IOException {
        String s3Key = s3Service.uploadFile(file);
        String url = s3Service.generateFileUrl(s3Key);

        return saveFileMetadata(name, description, s3Key, url);
    }

    public FileMetadata saveFileMetadata(String name, String description, String s3Key, String url) {
        FileMetadata fileMetadata = new FileMetadata(name, description, s3Key, url, LocalDateTime.now());
        return fileMetadataRepository.save(fileMetadata);
    }
}
