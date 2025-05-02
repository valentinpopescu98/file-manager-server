package com.valentin.demo_aws.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "file_metadata", indexes = @Index(name = "key_idx", columnList = "key"))
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 512)
    private String name;
    @Column(length = 2048)
    private String description;
    @Column(length = 512)
    private String key;
    @Column(columnDefinition = "TEXT") // very high character limit for lengthy URLs
    private String url;
    private LocalDateTime uploadedAt;

    public FileMetadata(String name, String description, String key, String url, LocalDateTime uploadedAt) {
        this.name = name;
        this.description = description;
        this.key = key;
        this.url = url;
        this.uploadedAt = uploadedAt;
    }
}
