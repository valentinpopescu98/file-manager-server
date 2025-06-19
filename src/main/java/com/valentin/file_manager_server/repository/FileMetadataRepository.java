package com.valentin.file_manager_server.repository;

import com.valentin.file_manager_server.model.FileMetadata;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByS3Key(String s3Key);
    @Transactional
    void deleteByS3Key(String s3Key);
}
