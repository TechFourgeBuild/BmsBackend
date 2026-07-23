package com.bms.BMSProject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    private String email;
    private String password;
}/* // 🎯 login() method ab REDUNDANT hai — Spring Security ka
    // AuthenticationManager + DaoAuthenticationProvider + CustomUserDetailsService
    // yehi kaam automatically karenge (yaad kar AuthController.login() pattern)
    // Ise DELETE kar sakta hai, ya sirf profile-fetch ke liye rakh sakta hai*/