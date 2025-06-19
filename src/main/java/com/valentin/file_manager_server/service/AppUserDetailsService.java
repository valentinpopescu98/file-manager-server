package com.valentin.file_manager_server.service;

import com.valentin.file_manager_server.model.AppUser;
import com.valentin.file_manager_server.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PublisherEmailVerificationService emailVerificationService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return User.builder()
                .username(appUser.getEmail())
                .password(appUser.getPassword())
                .roles(appUser.getRole())
                .build();
    }

    @Transactional
    public AppUser createRegularUser(String email, String password) {
        emailVerificationService.verifyEmail(email);

        AppUser newUser = new AppUser();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setFromOAuth2(false);
        return userRepository.save(newUser);
    }

    @Transactional
    private AppUser createOAuth2User(String email) {
        emailVerificationService.verifyEmail(email);

        AppUser newUser = new AppUser();
        newUser.setEmail(email);
        newUser.setPassword("");
        newUser.setFromOAuth2(true);
        return userRepository.save(newUser);
    }

    @Transactional
    public AppUser getOrCreateOAuth2User(String email) {
        return userRepository.findByEmail(email)
            .orElseGet(() -> createOAuth2User(email));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public AppUser save(AppUser user) {
        return userRepository.save(user);
    }

    public boolean passwordMismatch(String raw, String encoded) {
        return !passwordEncoder.matches(raw, encoded);
    }
}
