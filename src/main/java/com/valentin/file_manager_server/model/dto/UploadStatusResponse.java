package com.valentin.file_manager_server.model.dto;

import com.valentin.file_manager_server.model.enums.UploadStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UploadStatusResponse {

    private UploadStatus status;
}
