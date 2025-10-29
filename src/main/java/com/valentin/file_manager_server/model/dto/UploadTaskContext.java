package com.valentin.file_manager_server.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.File;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UploadTaskContext {

    private String uploadId;
    private String originalName; // name without prefixes (original name)
    private File content;
}
