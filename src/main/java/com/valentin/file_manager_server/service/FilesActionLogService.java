package com.valentin.file_manager_server.service;

import com.valentin.file_manager_server.model.FilesActionLog;
import com.valentin.file_manager_server.model.enums.FileAction;
import com.valentin.file_manager_server.repository.FilesActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class FilesActionLogService {

    private final FilesActionLogRepository filesActionLogRepository;

    public Instant getLastUploadOrDeleteTimestamp() {
        return filesActionLogRepository.findLastMutationTimestamp(FileAction.mutations());
    }

    public void logAction(FileAction action, String author, Long fileMetadataId) {
        FilesActionLog log = new FilesActionLog();
        log.setAction(action);
        log.setAuthor(author);
        log.setFileMetadataId(fileMetadataId);
        log.setTimestamp(Instant.now());

        filesActionLogRepository.save(log);
    }
}
