package com.example.carrental.service;

import com.example.carrental.dto.UserProfileDto;
import com.example.carrental.dto.UserRegistrationDto;
import com.example.carrental.entity.Role;
import com.example.carrental.entity.User;

import java.util.List;

public interface UserService {

    User registerUser(UserRegistrationDto registrationDto);

    User findById(Long id);

    User findByUsername(String username);

    User findByEmail(String email);

    User updateUserProfile(Long userId, UserProfileDto profileDto);

    List<User> findAllUsers();

    List<User> findUsersByRole(Role role);

    void toggleUserStatus(Long userId);

    long countTotalUsers();
}
