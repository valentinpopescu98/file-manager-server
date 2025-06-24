package com.valentin.file_manager_server.repository;

import com.valentin.file_manager_server.model.FileMetadata;
import org.springframework.data.jpa.domain.Specification;

public class FileMetadataSpecification {

    public static Specification<FileMetadata> filterByName(String name) {
        if (name == null || name.isEmpty()) return null;

        String pattern = "%" + name.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<FileMetadata> filterByDescription(String description) {
        if (description == null || description.isEmpty()) return null;

        String pattern = "%" + description.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("description")), pattern);
    }

    public static Specification<FileMetadata> filterByUploaderEmail(String email) {
        if (email == null || email.isEmpty()) return null;

        String pattern = "%" + email.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("uploaderEmail")), pattern);
    }

    public static Specification<FileMetadata> filterByUploadedAt(String uploadedAt) {
        if (uploadedAt == null || uploadedAt.isEmpty()) return null;

        return (root, query, cb) -> {
            String pattern = "%" + uploadedAt.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("uploadedAt").as(String.class)), pattern);
        };
    }
}
