package com.smartcanteen.service;

import com.smartcanteen.model.Role;
import com.smartcanteen.model.User;
import com.smartcanteen.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerStudent(String fullName, String username, String rawPassword) {
        String trimmedUsername = username == null ? "" : username.trim();
        String trimmedName = fullName == null ? "" : fullName.trim();

        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (trimmedUsername.length() < 3 || trimmedUsername.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (userRepository.existsByUsername(trimmedUsername)) {
            throw new IllegalArgumentException("Username '" + trimmedUsername + "' is already taken.");
        }

        User user = new User(
                trimmedName,
                trimmedUsername,
                passwordEncoder.encode(rawPassword),
                Role.STUDENT.name()
        );
        return userRepository.save(user);
    }
}
