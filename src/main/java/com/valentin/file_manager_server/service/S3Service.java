package com.valentin.file_manager_server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class S3Service {

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public ResponseInputStream<GetObjectResponse> downloadFile(String s3Key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
        return s3Client.getObject(request);
    }

    public String uploadFile(File file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getName();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        try (InputStream inputStream = new FileInputStream(file)) {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.length()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }

        return fileName;
    }

    public String generateFileUrl(String s3Key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public void deleteFile(String s3Key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    public String extractOriginalFilename(String s3Key) {
        // Remove first 3 prefixes
        int temp = s3Key.indexOf("_");
        int tempUUID = s3Key.indexOf("_", temp + 1);
        int s3UUID = s3Key.indexOf("_", tempUUID + 1);

        if (temp == -1 || tempUUID == -1 || s3UUID == -1 || s3UUID + 1 >= s3Key.length()) {
            return s3Key; // fallback if it doesn't follow structure
        }

        return s3Key.substring(s3UUID + 1);
    }
}
