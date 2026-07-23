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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private static final String ADMIN_SECRET_KEY = "bookit-admin-2026";
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

    public AuthResponse login(LoginRequest request)
    {
        User user2=userRepository.findByEmail(request.getEmail())
                .orElseThrow(()->new ResourceNotFoundException("User not found with email: " + request.getEmail()));
        if(!passwordEncoder.matches(request.getPassword(), user2.getPassword()))
        {
            throw new InvalidCredentialsException("Invalid password");
        }

        // 3️⃣ Generate JWT token for this user
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user2.getEmail(),
                user2.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user2.getRole().name()))
        );
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse (token,user2);
    }

    /* // 🎯 login() method ab REDUNDANT hai — Spring Security ka
    // AuthenticationManager + DaoAuthenticationProvider + CustomUserDetailsService
    // yehi kaam automatically karenge (yaad kar AuthController.login() pattern)
    // Ise DELETE kar sakta hai, ya sirf profile-fetch ke liye rakh sakta hai*/

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getUserById(Long id)
    {
        return userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with id: " + id));
    }
}