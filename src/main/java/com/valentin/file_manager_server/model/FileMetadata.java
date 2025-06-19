package com.valentin.file_manager_server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 512)
    private String name;
    @Column(length = 2048)
    private String description;
    @Column(nullable = false, length = 256)
    private String uploaderEmail;
    @Column(name = "s3_key", length = 512)
    private String s3Key;
    @Column(length = 4096)
    private String url;
    private LocalDateTime uploadedAt;

    public FileMetadata(String name, String description, String uploaderEmail,
                        String s3Key, String url, LocalDateTime uploadedAt) {
        this.name = name;
        this.description = description;
        this.uploaderEmail = uploaderEmail;
        this.s3Key = s3Key;
        this.url = url;
        this.uploadedAt = uploadedAt;
    }
}
