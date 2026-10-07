package com.example.carrental.service;

import com.example.carrental.dto.UserRegistrationDto;
import com.example.carrental.entity.Role;
import com.example.carrental.entity.User;
import com.example.carrental.repository.UserRepository;
import com.example.carrental.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void testRegisterUser_Success() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        dto.setFirstName("Test");
        dto.setLastName("User");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        User savedUser = userService.registerUser(dto);

        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUsername());
        assertEquals(Role.CUSTOMER, savedUser.getRole());
        assertTrue(savedUser.getIsActive());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_DuplicateUsername() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("existinguser");
        dto.setEmail("new@example.com");
        dto.setPassword("pass123");
        dto.setConfirmPassword("pass123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_PasswordMismatch() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("newuser");
        dto.setEmail("new@example.com");
        dto.setPassword("secret1");
        dto.setConfirmPassword("secret2");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any(User.class));
    }
}
