package com.ad.adchat.service;

import com.ad.adchat.model.User;
import com.ad.adchat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Service for user-related operations, including registration and loading user details for authentication.
 */
@Service
@RequiredArgsConstructor // We can now use @RequiredArgsConstructor for all final fields
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // This is now safely injected from PasswordEncoderConfig

    /**
     * Registers a new user.
     * @param username The username.
     * @param password The raw password.
     * @return The saved User entity.
     * @throws RuntimeException if the username already exists.
     */
    @Transactional
    public User registerUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // Hash the password
        return userRepository.save(user);
    }

    /**
     * Loads user details by username for Spring Security.
     * @param username The username to load.
     * @return UserDetails object containing user info.
     * @throws UsernameNotFoundException if the user is not found.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Spring Security User (implements UserDetails)
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>() // Empty list for authorities/roles for now
        );
    }
}

