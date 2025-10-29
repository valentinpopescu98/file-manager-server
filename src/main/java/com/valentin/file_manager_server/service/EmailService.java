package com.valentin.file_manager_server.service;

import com.valentin.file_manager_server.model.FileMetadata;
import com.valentin.file_manager_server.model.dto.UploadResult;
import com.valentin.file_manager_server.model.enums.UploadStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EmailService {

    @Value("${aws.ses.publisher.email}")
    private String publisherEmail;

    private final SesClient sesClient;

    @Async
    public void sendDownloadNotification(FileMetadata metadata, String actionBy) {
        String subject = "File downloaded";
        String body = String.format("""
            Hello,

            File "%s" was downloaded.

            Description: %s
            Uploaded at: %s
            
            Action performed by: %s
            
            Best regards!
            """, metadata.getName(), metadata.getDescription(), metadata.getUploadedAt(), actionBy);

        sendEmail(metadata.getUploaderEmail(), subject, body);
    }

    @Async
    public void sendUploadConfirmationEmail(List<UploadResult> metadata, String actionBy) {
        String subject = "Files upload summary";
        String body = String.format("""
            Hello,

            The following files were uploaded:
            
            %s

            Action performed by: %s
            
            Best regards!
            """, metadata.stream().map(this::uploadResToStr).collect(Collectors.joining("\n")), actionBy);

        sendEmail(actionBy, subject, body);
    }

    @Async
    public void sendDeleteNotification(FileMetadata metadata, String actionBy) {
        String subject = "File deleted";
        String body = String.format("""
            Hello,

            File "%s" was deleted from the server.

            Description: %s
            Uploaded at: %s
            
            Action performed by: %s
            
            Best regards!
            """, metadata.getName(), metadata.getDescription(), metadata.getUploadedAt(), actionBy);

        sendEmail(metadata.getUploaderEmail(), subject, body);
    }

    private String uploadResToStr(UploadResult result) {
        return String.format("""
                Name: %s
                Description: %s
                Uploaded at: %s
                Status: %s
                """,
                result.getName(),
                result.getDescription(),
                result.getUploadedAt() == null ? "--" : result.getUploadedAt(),
                result.getStatus() == UploadStatus.DONE ? "SUCCESS" : "FAILED");
    }

    private void sendEmail(String to, String subject, String body) {
        SendEmailRequest request = SendEmailRequest.builder()
                .destination(Destination.builder().toAddresses(to).build())
                .message(Message.builder()
                        .subject(Content.builder().data(subject).build())
                        .body(Body.builder().text(Content.builder().data(body).build()).build())
                        .build())
                .source(publisherEmail)
                .build();

        try {
            sesClient.sendEmail(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
