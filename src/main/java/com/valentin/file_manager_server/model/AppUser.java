package com.valentin.file_manager_server.model;

import com.valentin.file_manager_server.model.enums.AccountStatus;
import com.valentin.file_manager_server.model.enums.AuthorizationRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static com.valentin.file_manager_server.model.enums.AccountStatus.SUSPENDED;
import static com.valentin.file_manager_server.model.enums.AuthorizationRole.USER;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    @Column(name = "from_oauth2", nullable = false)
    private boolean fromOAuth2 = false;
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AuthorizationRole role = USER;
    @Column(name = "account_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus = SUSPENDED;

    public UserDetails toUserDetails() {
        return User.builder()
                .username(email)
                .password(password)
                .roles(role.toString())
                .build();
    }
}
