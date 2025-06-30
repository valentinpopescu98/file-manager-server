package com.valentin.file_manager_server.model;

import com.valentin.file_manager_server.model.enums.FileAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "files_action_log", indexes = {
        @Index(name = "idx_action_timestamp", columnList = "action, timestamp"),
        @Index(name = "idx_author", columnList = "author"),
        @Index(name = "idx_file_metadata", columnList = "fileMetadataId")
})
public class FilesActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private FileAction action; // "UPLOAD", "DELETE", "DOWNLOAD"
    @Column(nullable = false)
    private Instant timestamp = Instant.now();
    @Column(nullable = false, length = 255)
    private String author;
    @Column(nullable = false)
    private Long fileMetadataId;
}
