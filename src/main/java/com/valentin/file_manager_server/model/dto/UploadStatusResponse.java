package com.valentin.file_manager_server.model.dto;

import com.valentin.file_manager_server.model.enums.UploadStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UploadStatusResponse {

    private UploadStatus status;
}
