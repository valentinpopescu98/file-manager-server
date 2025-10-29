package com.valentin.file_manager_server.model.dto;

import com.valentin.file_manager_server.model.enums.UploadStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UploadResult {

    private String name;
    private String description;
    private LocalDateTime uploadedAt;
    private UploadStatus status;
}
