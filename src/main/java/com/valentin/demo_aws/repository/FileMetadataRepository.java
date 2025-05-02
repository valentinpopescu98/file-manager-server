package com.valentin.demo_aws.repository;

import com.valentin.demo_aws.model.FileMetadata;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadataKeyProjection> findAllBy();
    Optional<FileMetadata> findByKey(String key);
    @Transactional
    void deleteByKey(String key);
}
