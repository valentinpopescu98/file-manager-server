package com.valentin.file_manager_server.service;

import com.valentin.file_manager_server.model.AppUser;
import com.valentin.file_manager_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
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

    public AppUser createRegularUser(String email, String password) {
        AppUser newUser = new AppUser();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setFromOAuth2(false);
        return userRepository.save(newUser);
    }

    public AppUser createOAuth2UserIfNotPresent(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    AppUser newUser = new AppUser();
                    newUser.setEmail(email);
                    newUser.setPassword("");
                    newUser.setFromOAuth2(true);
                    return userRepository.save(newUser);
                });
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
