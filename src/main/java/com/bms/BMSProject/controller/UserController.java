package com.bms.BMSProject.controller;


import com.bms.BMSProject.dto.AuthResponse;
import com.bms.BMSProject.dto.LoginRequest;
import com.bms.BMSProject.dto.UserRequest;
import com.bms.BMSProject.entity.User;
import com.bms.BMSProject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserRequest request)
    {
        return  ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request)
    {
        AuthResponse response = userService.login(request);
        return  ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /* // 🎯 login() method ab REDUNDANT hai — Spring Security ka
    // AuthenticationManager + DaoAuthenticationProvider + CustomUserDetailsService
    // yehi kaam automatically karenge (yaad kar AuthController.login() pattern)
    // Ise DELETE kar sakta hai, ya sirf profile-fetch ke liye rakh sakta hai*/

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers()
    {
        return  ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id)
    {
        return  ResponseEntity.ok(userService.getUserById(id));
    }
}