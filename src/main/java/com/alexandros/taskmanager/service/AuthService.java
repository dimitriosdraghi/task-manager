package com.alexandros.taskmanager.service;

import com.alexandros.taskmanager.dto.AuthResponse;
import com.alexandros.taskmanager.dto.LoginRequest;
import com.alexandros.taskmanager.dto.RegisterRequest;
import com.alexandros.taskmanager.dto.UserResponse;
import com.alexandros.taskmanager.entity.Role;
import com.alexandros.taskmanager.entity.User;
import com.alexandros.taskmanager.exception.UsernameAlreadyExistsException;
import com.alexandros.taskmanager.repository.UserRepository;
import com.alexandros.taskmanager.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }
        String passwordHash = passwordEncoder.encode(request.password());
        // New accounts are always USER; ADMIN is granted manually
        User user = new User(request.username(), passwordHash, Role.USER);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Same message for unknown user and wrong password, so we don't reveal which usernames exist
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}
