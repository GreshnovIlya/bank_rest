package com.example.bankcards.service;

import com.example.bankcards.dto.AuthorizationRequest;
import com.example.bankcards.dto.RegistrationRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UserException;
import com.example.bankcards.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorizationServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthorizationService authorizationService;

    User user = new User(1L, "Anton", "bgyfygvbhjnug", Role.USER);
    UserDetails userDetails = new User(1L, "Anton", "bgyfygvbhjnug", Role.USER);
    RegistrationRequest registrationRequest = new RegistrationRequest("Anton", "password", Role.USER);
    AuthorizationRequest authorizationRequest = new AuthorizationRequest("Anton", "password");

    @Test
    void testRegistration_Successful() {
        when(userService.createUser(any(User.class)))
                .thenReturn(user);
        when(passwordEncoder.encode(registrationRequest.getPassword()))
                .thenReturn(user.getPassword());
        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("token");

        String token = authorizationService.registration(registrationRequest);

        assertEquals("token", token);

        verify(userService).createUser(argThat(user -> user.getUsername().equals("Anton")
                && user.getPassword().equals("bgyfygvbhjnug") && user.getRole() == Role.USER && user.getId() == null));
        verify(passwordEncoder).encode("password");
        verify(jwtService).generateToken(any(UserDetails.class));
    }

    @Test
    void testRegistration_UserWithUsernameExists() {
        when(userService.createUser(any(User.class)))
                .thenThrow(new UserException("Пользователь с таким именем уже существует"));
        when(passwordEncoder.encode(registrationRequest.getPassword()))
                .thenReturn(user.getPassword());

        assertThrows(UserException.class, () -> authorizationService.registration(registrationRequest));

        verify(userService).createUser(any(User.class));
    }

    @Test
    void testAuthorization_Successful() {
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        when(userService.userDetailsService())
                .thenReturn(userDetailsService);
        when(userService.userDetailsService().loadUserByUsername("Anton"))
                .thenReturn(userDetails);
        when(jwtService.generateToken(userDetails))
                .thenReturn("token-new");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(any(Authentication.class));

        String token = authorizationService.authorization(authorizationRequest);

        assertEquals("token-new", token);

        verify(userDetailsService).loadUserByUsername("Anton");
        verify(jwtService).generateToken(userDetails);
        verify(authenticationManager).authenticate(argThat(authorization ->
                authorization.getPrincipal().equals("Anton")));
    }

    @Test
    void testAuthorization_InvalidPassword() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid password"));

        assertThrows(BadCredentialsException.class, () -> {
            authorizationService.authorization(authorizationRequest);
        });

        verify(authenticationManager).authenticate(any());
        verify(userService, never()).userDetailsService();
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void testAuthorization_UserNotExist() {
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        when(userService.userDetailsService())
                .thenReturn(userDetailsService);
        when(userService.userDetailsService().loadUserByUsername(eq("Anton")))
                .thenThrow(new NotFoundException("Пользователь не найден"));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(any(Authentication.class));

        assertThrows(NotFoundException.class, () -> authorizationService.authorization(authorizationRequest));

        verify(authenticationManager).authenticate(any());
        verify(userService.userDetailsService()).loadUserByUsername("Anton");
        verify(jwtService, never()).generateToken(any());
    }
}
