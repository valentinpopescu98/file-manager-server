package com.valentin.file_manager_server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "app_user")
public class AppUser {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String email;
        private String password;
        private String role = "USER";
        private boolean fromOAuth2 = false;

        public UserDetails toUserDetails() {
                return User.builder()
                        .username(email)
                        .password(password)
                        .roles(role)
                        .build();
        }
}
