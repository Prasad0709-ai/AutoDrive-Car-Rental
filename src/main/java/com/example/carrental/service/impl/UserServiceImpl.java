package com.example.carrental.service.impl;

import com.example.carrental.dto.UserProfileDto;
import com.example.carrental.dto.UserRegistrationDto;
import com.example.carrental.entity.Role;
import com.example.carrental.entity.User;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.UserRepository;
import com.example.carrental.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username '" + dto.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already registered.");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName() != null ? dto.getFirstName().trim() : "");
        user.setLastName(dto.getLastName() != null ? dto.getLastName().trim() : "");
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setDriversLicense(dto.getDriversLicense());
        user.setRole(Role.CUSTOMER);
        user.setIsActive(true);

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    public User updateUserProfile(Long userId, UserProfileDto profileDto) {
        User user = findById(userId);

        if (!user.getEmail().equalsIgnoreCase(profileDto.getEmail())) {
            if (userRepository.existsByEmail(profileDto.getEmail())) {
                throw new IllegalArgumentException("Email '" + profileDto.getEmail() + "' is already in use.");
            }
            user.setEmail(profileDto.getEmail().trim().toLowerCase());
        }

        user.setFirstName(profileDto.getFirstName() != null ? profileDto.getFirstName().trim() : "");
        user.setLastName(profileDto.getLastName() != null ? profileDto.getLastName().trim() : "");
        user.setPhone(profileDto.getPhone());
        user.setAddress(profileDto.getAddress());
        user.setDriversLicense(profileDto.getDriversLicense());

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Override
    public void toggleUserStatus(Long userId) {
        User user = findById(userId);
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalUsers() {
        return userRepository.count();
    }
}
