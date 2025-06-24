package com.valentin.file_manager_server.repository;

import com.valentin.file_manager_server.model.FileMetadata;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class FileMetadataSpecification {

    public static Specification<FileMetadata> nameContains(String name) {
        if (name == null || name.isEmpty()) return null;

        String pattern = "%" + name.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<FileMetadata> descriptionContains(String description) {
        if (description == null || description.isEmpty()) return null;

        String pattern = "%" + description.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("description")), pattern);
    }

    public static Specification<FileMetadata> uploaderEmailContains(String email) {
        if (email == null || email.isEmpty()) return null;

        String pattern = "%" + email.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("uploaderEmail")), pattern);
    }

    public static Specification<FileMetadata> uploadedAtBetween(LocalDate before, LocalDate after) {
        return (root, query, cb) -> {
            Path<LocalDateTime> uploadedAt = root.get("uploadedAt");
            List<Predicate> predicates = new ArrayList<>();

            if (before != null) {
                // Convert LocalDate to LocalDateTime at end of day
                LocalDateTime beforeEnd = before.atTime(LocalTime.MAX);
                predicates.add(cb.lessThanOrEqualTo(uploadedAt, beforeEnd));
            }
            if (after != null) {
                // Convert LocalDate to LocalDateTime at start of day
                LocalDateTime afterStart = after.atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(uploadedAt, afterStart));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
