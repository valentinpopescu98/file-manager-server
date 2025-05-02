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
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 512)
    private String name;
    @Column(length = 2048)
    private String description;
    @Column(length = 512)
    private String s3Key;
    @Column(columnDefinition = "TEXT") // very high character limit for lengthy URLs
    private String url;
    private LocalDateTime uploadedAt;

    public FileMetadata(String name, String description, String s3Key, String url, LocalDateTime uploadedAt) {
        this.name = name;
        this.description = description;
        this.s3Key = s3Key;
        this.url = url;
        this.uploadedAt = uploadedAt;
    }
}
