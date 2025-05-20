package com.valentin.file_manager_server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.VerifyEmailIdentityRequest;

@RequiredArgsConstructor
@Service
public class PublisherEmailVerificationService {

    private final SesClient sesClient;

    public void verifyEmail(String email) {
        VerifyEmailIdentityRequest request = VerifyEmailIdentityRequest.builder()
                .emailAddress(email)
                .build();
        sesClient.verifyEmailIdentity(request);
    }
}
