package com.valentin.file_manager_server.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponse {

    private String uploadId;
    private String originalName; // name without prefixes (original name)
}
