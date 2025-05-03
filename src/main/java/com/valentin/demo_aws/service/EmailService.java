package com.valentin.demo_aws.service;

import com.valentin.demo_aws.model.FileMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class EmailService {

    @Value("${aws.ses.publisher.email}")
    private String publisherEmail;

    @Autowired
    private SesClient sesClient;

    public void sendDownloadNotification(FileMetadata metadata, String actionBy) {
        String subject = "File downloaded";
        String body = String.format("""
            Hello,

            File "%s" was downloaded.

            Description: %s
            Uploaded at: %s
            
            Action was made by: %s
            
            Best regards!
            """, metadata.getName(), metadata.getDescription(), metadata.getUploadedAt(), actionBy);

        sendEmail(metadata.getUploaderEmail(), subject, body);
    }

    public void sendUploadConfirmationEmail(FileMetadata metadata, String actionBy) {
        String subject = "File uploaded successfully";
        String body = String.format("""
            Hello,

            File "%s" was uploaded.

            Description: %s
            Uploaded at: %s

            Action was made by: %s
            
            Best regards!
            """, metadata.getName(), metadata.getDescription(), metadata.getUploadedAt(), actionBy);

        sendEmail(metadata.getUploaderEmail(), subject, body);
    }

    public void sendDeleteNotification(FileMetadata metadata, String actionBy) {
        String subject = "File deleted";
        String body = String.format("""
            Hello,

            File "%s" was deleted from the server.

            Description: %s
            Uploaded at: %s
            
            Action was made by: %s
            
            Best regards!
            """, metadata.getName(), metadata.getDescription(), metadata.getUploadedAt(), actionBy);

        sendEmail(metadata.getUploaderEmail(), subject, body);
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

        sesClient.sendEmail(request);
    }
}
