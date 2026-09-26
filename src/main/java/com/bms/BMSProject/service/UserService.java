package com.bms.BMSProject.service;

import com.bms.BMSProject.dto.AuthResponse;
import com.bms.BMSProject.dto.LoginRequest;
import com.bms.BMSProject.dto.UserRequest;
import com.bms.BMSProject.entity.User;
import com.bms.BMSProject.enums.Role;
import com.bms.BMSProject.exception.DuplicateResourceException;
import com.bms.BMSProject.exception.InvalidCredentialsException;
import com.bms.BMSProject.exception.ResourceNotFoundException;
import com.bms.BMSProject.repository.UserRepository;
import com.bms.BMSProject.security.jwt.JwtUtil;
import com.bms.BMSProject.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private static final String ADMIN_SECRET_KEY = "bookit-admin-2026";
    private final AuthenticationManager authenticationManager;
    private final CookieUtil cookieUtil;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationMs;

    //register
    public User register(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email '" + request.getEmail() + "' already exists");
        }

        Role role = Role.USER;
        if (request.getSecretKey() != null && request.getSecretKey().equals(ADMIN_SECRET_KEY)) {
            role = Role.ADMIN;
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .build();

        return userRepository.save(user);
    }

    //login
    public AuthResponse login(LoginRequest request, HttpServletResponse response)
    {
        User user2=userRepository.findByEmail(request.getEmail())
                .orElseThrow(()->new ResourceNotFoundException("User not found with email: " + request.getEmail()));
        if(!passwordEncoder.matches(request.getPassword(), user2.getPassword()))
        {
            throw new InvalidCredentialsException("Invalid password");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        LocalDateTime expiryOfRefreshToken = LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs));

        String token = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername(), expiryOfRefreshToken);

        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        return new AuthResponse (token,user2);
    }

    public Map<String,String> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String oldRefreshToken = cookieUtil.getRefreshTokenFromCookie(request);
        if(oldRefreshToken == null ){
            throw new RuntimeException("Session expired. Please sign in again.");
        }

        if (!jwtUtil.validateRefreshToken(oldRefreshToken)) {
            cookieUtil.clearRefreshTokenCookie(response);
            throw new RuntimeException("Invalid or expired refresh token. Please sign in again.");
        }

        String email = jwtUtil.extractUsername(oldRefreshToken, true);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        String newAccessToken = jwtUtil.generateToken(userDetails);

        LocalDateTime newExpiry = LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs));
        String newRefreshToken = jwtUtil.generateRefreshToken(email, newExpiry);

        cookieUtil.addRefreshTokenCookie(response, newRefreshToken);

        return Map.of(
                "accessToken", newAccessToken,
                "email", email
        );
    }

    public void logout(HttpServletResponse response) {
        cookieUtil.clearRefreshTokenCookie(response);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getUserById(Long id)
    {
        return userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with id: " + id));
    }
}