package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.LoginRequest;
import com.anup.restaurant_backend.dto.UserRequestDto;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.exception.ValidationException;
import com.anup.restaurant_backend.exception.ValidationUtil;
import com.anup.restaurant_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String registerUser(UserRequestDto dto) {

        // ── VALIDATION ──────────────────────────────────────
        // These throw ValidationException → GlobalHandler → { "message": "..." }

        ValidationUtil.requireNotBlank(dto.getFirstName(), "First name");
        ValidationUtil.requireNotBlank(dto.getLastName(),  "Last name");
        ValidationUtil.requireNotBlank(dto.getEmail(),     "Email");
        ValidationUtil.requireValidEmail(dto.getEmail());
        ValidationUtil.requireNotBlank(dto.getPhone(),     "Phone number");
        ValidationUtil.requireValidPhone(dto.getPhone());
        ValidationUtil.requireNotBlank(dto.getPassword(),  "Password");
        ValidationUtil.requireMinLength(dto.getPassword(), 6, "Password");
        ValidationUtil.requireNotNull(dto.getRole(), "Role");

        // ── DUPLICATE EMAIL CHECK ────────────────────────────
        Optional<UserEntity> existing = userRepository.findByEmail(dto.getEmail().trim());
        if (existing.isPresent()) {
            throw new ValidationException("This email is already registered. Please sign in.");
        }

        // ── CREATE USER ──────────────────────────────────────
        UserEntity user = new UserEntity();
        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone().trim());
        user.setRole(dto.getRole());
        user.setWalletBalance(1000.0); // free wallet credit on registration

        userRepository.save(user);

        log.info("New user registered: {}", user.getEmail());
        return "User registered successfully";
    }

    @Override
    public String loginUser(LoginRequest dto) {

        // ── VALIDATION ──────────────────────────────────────
        ValidationUtil.requireNotBlank(dto.getEmail(),    "Email");
        ValidationUtil.requireNotBlank(dto.getPassword(), "Password");

        // ── FIND USER ────────────────────────────────────────
        UserEntity user = userRepository.findByEmail(dto.getEmail().trim())
                .orElseThrow(() -> new ValidationException("No account found with this email"));

        // ── CHECK PASSWORD ───────────────────────────────────
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ValidationException("Incorrect password. Please try again.");
        }

        log.info("User logged in: {}", user.getEmail());
        return "Login successful";
    }
}