package com.valentin.file_manager_server.repository;

import com.valentin.file_manager_server.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByEmail(String email);
    Optional<AppUser> findByEmail(String email);
}
