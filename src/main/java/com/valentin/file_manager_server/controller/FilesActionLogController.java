package com.valentin.file_manager_server.controller;

import com.valentin.file_manager_server.service.FilesActionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/log/files/actions")
public class FilesActionLogController {

    private final FilesActionLogService filesActionLogService;

    @GetMapping("/last-mutation")
    public ResponseEntity<Instant> getLastMutation() {
        Instant last = filesActionLogService.getLastUploadOrDeleteTimestamp();
        return ResponseEntity.ok(last != null ? last : Instant.EPOCH);
    }
}
