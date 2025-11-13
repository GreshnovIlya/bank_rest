package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UserException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    User newUser = new User(null, "Anton", "bgyfygvbhjnug", Role.USER);
    User user = new User(1L, "Anton", "bgyfygvbhjnug", Role.USER);

    @Test
    void createUser_Successful() {
        when(userRepository.existsByUsername(newUser.getUsername()))
                .thenReturn(false);
        when(userRepository.save(newUser))
                .thenReturn(user);

        User anton = userService.createUser(newUser);

        assertEquals(anton, user);

        verify(userRepository).existsByUsername(user.getUsername());
        verify(userRepository).save(newUser);
    }

    @Test
    void createUser_UserWithNameExist() {
        when(userRepository.existsByUsername(newUser.getUsername()))
                .thenReturn(true);

        assertThrows(UserException.class, () -> userService.createUser(newUser));

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUsers_Successful() {
        Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

        when(userRepository.findAll(PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(page);

        List<UserDto> users = userService.getUsers(0, 10, "id", "asc");

        assertEquals(users.size(), 1);
        assertEquals(users.get(0).getUsername(), user.getUsername());
        assertEquals(users.get(0).getRole(), user.getRole());

        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void deleteUser_Successful() {
        when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Optional.of(user));

        userService.deleteUser(user.getUsername());

        verify(userRepository).findByUsername(user.getUsername());
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_UserNotFound() {
        when(userRepository.findByUsername("igorr"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUser("igorr"));

        verify(userRepository).findByUsername("igorr");
        verify(userRepository, never()).delete(any());
    }

    @Test
    void userDetailsService_Successful() {
        UserDetailsService userDetailsService = userService.userDetailsService();

        when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());

        verify(userRepository).findByUsername(user.getUsername());
    }

    @Test
    void getByUsername_Successful() {
        when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Optional.of(user));

        User anton = userService.getByUsername(user.getUsername());

        assertEquals(user, anton);

        verify(userRepository).findByUsername(user.getUsername());
    }

    @Test
    void getByUsername_UserNotFound() {
        when(userRepository.findByUsername("igorr"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getByUsername("igorr"));

        verify(userRepository).findByUsername("igorr");
    }

    @Test
    void getCurrentUser_Successful() {
        SecurityContext context = mock(SecurityContext.class);
        SecurityContextHolder.setContext(context);

        when(context.getAuthentication())
                .thenReturn(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Optional.of(user));

        User anton = userService.getCurrentUser();

        assertEquals(user, anton);

        verify(userRepository).findByUsername(user.getUsername());

        SecurityContextHolder.clearContext();
    }
}
