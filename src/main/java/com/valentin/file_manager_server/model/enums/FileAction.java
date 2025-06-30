package com.valentin.file_manager_server.model.enums;

import java.util.List;

public enum FileAction {

    DOWNLOAD,
    UPLOAD,
    DELETE;

    public static List<FileAction> mutations() {
        return List.of(UPLOAD, DELETE);
    }
}
