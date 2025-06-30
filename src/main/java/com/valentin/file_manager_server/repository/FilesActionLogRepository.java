package com.valentin.file_manager_server.repository;

import com.valentin.file_manager_server.model.FilesActionLog;
import com.valentin.file_manager_server.model.enums.FileAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface FilesActionLogRepository extends JpaRepository<FilesActionLog, Long> {

    @Query("SELECT MAX(f.timestamp) FROM FilesActionLog f WHERE f.action IN (:actions)")
    Instant findLastMutationTimestamp(@Param("actions") List<FileAction> actions);
}
