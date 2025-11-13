package com.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.bankcards.dto.AuthorizationRequest;
import com.example.bankcards.dto.RegistrationRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthorizationService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public String registration(RegistrationRequest request) {
        User user = new User(null, request.getUsername(), passwordEncoder.encode(request.getPassword()),
                request.getRole());

        return jwtService.generateToken(userService.createUser(user));
    }

    public String authorization(AuthorizationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));
        UserDetails user = userService.userDetailsService()
                                      .loadUserByUsername(request.getUsername());

        return jwtService.generateToken(user);
    }
}