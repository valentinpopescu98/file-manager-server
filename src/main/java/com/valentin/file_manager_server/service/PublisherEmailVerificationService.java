package com.valentin.file_manager_server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.VerifyEmailIdentityRequest;

@Service
public class PublisherEmailVerificationService {

    @Autowired
    private SesClient sesClient;

    public void verifyEmail(String email) {
        VerifyEmailIdentityRequest request = VerifyEmailIdentityRequest.builder()
                .emailAddress(email)
                .build();
        sesClient.verifyEmailIdentity(request);
    }
}
