package org.th.service.mobile;

import org.th.repository.*;
import org.th.entity.*;
import org.th.entity.shops.*;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.dto.mobile.AuthResponse;
import org.th.dto.mobile.LoginRequest;
import org.th.dto.mobile.RegisterRequest;
import org.th.entity.enums.Role;
import org.th.entity.User;
import org.th.repository.UserRepository;
import org.th.config.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;
        private final AuthenticationManager authenticationManager;

        @Transactional
        public AuthResponse register(RegisterRequest request) {
                // Check if username exists
                if (userRepository.existsByUsername(request.username())) {
                        throw new org.th.exception.ApplicationException(org.th.exception.ErrorCode.USER_ALREADY_EXISTS,
                                        "Username already exists");
                }

                // Check if email exists
                if (userRepository.existsByEmail(request.email())) {
                        throw new org.th.exception.ApplicationException(org.th.exception.ErrorCode.EMAIL_ALREADY_EXISTS,
                                        "Email already exists");
                }

                // Create new user
                User user = new User();
                user.setUsername(request.username());
                user.setEmail(request.email());
                user.setPassword(passwordEncoder.encode(request.password()));
                user.setFullName(request.fullName());
                user.setRole(Role.USER);
                user.setAgreedToTermsAt(LocalDateTime.now());
                user.setPrivacyPolicyVersion(
                                request.privacyPolicyVersion() != null ? request.privacyPolicyVersion() : "v1.0");

                User savedUser = userRepository.save(user);

                // Authenticate and generate token
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String token = jwtTokenProvider.generateToken(authentication);

                return new AuthResponse(
                                token,
                                savedUser.getId(),
                                savedUser.getUsername(),
                                savedUser.getEmail(),
                                savedUser.getFullName(),
                                savedUser.getRole().name());
        }

        public AuthResponse login(LoginRequest request) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.usernameOrEmail(),
                                                request.password()));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String token = jwtTokenProvider.generateToken(authentication);

                User user = userRepository.findByUsernameOrEmail(request.usernameOrEmail(), request.usernameOrEmail())
                                .orElseThrow(() -> new org.th.exception.ApplicationException(
                                                org.th.exception.ErrorCode.USER_NOT_FOUND, "User not found"));

                return new AuthResponse(
                                token,
                                user.getId(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getFullName(),
                                user.getRole().name());
        }
}