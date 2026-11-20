package com.budgetbounty.service;

import com.budgetbounty.dto.auth.AuthResponse;
import com.budgetbounty.dto.auth.LoginRequest;
import com.budgetbounty.dto.auth.RegisterRequest;
import com.budgetbounty.dto.common.UserDto;
import com.budgetbounty.entity.User;
import com.budgetbounty.exception.BadRequestException;
import com.budgetbounty.repository.UserRepository;
import com.budgetbounty.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords must match.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("user")
                .rewardPoints(0)
                .build();
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole());
        return new AuthResponse(token, UserDto.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password."));

        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole());
        return new AuthResponse(token, UserDto.from(user));
    }

    public UserDto currentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found."));
        return UserDto.from(user);
    }
}
