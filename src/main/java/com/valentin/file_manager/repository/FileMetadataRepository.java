package com.valentin.file_manager.repository;

import com.valentin.file_manager.model.FileMetadata;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByKey(String key);
    @Transactional
    void deleteByKey(String key);
}
