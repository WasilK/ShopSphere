package com.wasil.ShopSphere.services;


import com.wasil.ShopSphere.dto.user.UserRequest;
import com.wasil.ShopSphere.dto.user.UserResponse;
import com.wasil.ShopSphere.exceptions.UserNotFoundException;
import com.wasil.ShopSphere.model.Role;
import com.wasil.ShopSphere.model.User;
import com.wasil.ShopSphere.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> findAllUsers() {
        return userRepository.findAll().stream().map(this::convertToResponse).toList();
    }

    public UserResponse findUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return convertToResponse(user);
    }

    public UserResponse addUser(UserRequest userRequest) {
        if (userRepository.findByUserEmail(userRequest.getUserEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setUserName(userRequest.getUserName());
        user.setUserEmail(userRequest.getUserEmail());
        user.setUserPassword(passwordEncoder.encode(userRequest.getUserPassword()));
        user.setUserPhone(userRequest.getUserPhone());
        user.setRole(Role.CUSTOMER);
        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    public UserResponse updateUser(Long id, UserRequest userRequest) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setUserName(userRequest.getUserName());
        user.setUserEmail(userRequest.getUserEmail());
        user.setUserPassword(passwordEncoder.encode(userRequest.getUserPassword()));
        user.setUserPhone(userRequest.getUserPhone());
        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    private UserResponse convertToResponse(User user){
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setUserName(user.getUserName());
        response.setUserEmail(user.getUserEmail());
        response.setUserPhone(user.getUserPhone());
        response.setUserCreatedAt(user.getUserCreatedAt());
        response.setUserUpdatedAt(user.getUserUpdatedAt());
        return response;
    }
}
