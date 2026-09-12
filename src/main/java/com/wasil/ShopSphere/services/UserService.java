package com.wasil.ShopSphere.services;


import com.wasil.ShopSphere.dto.user.ChangePasswordRequest;
import com.wasil.ShopSphere.dto.user.UpdateUserRequest;
import com.wasil.ShopSphere.dto.user.UserRequest;
import com.wasil.ShopSphere.dto.user.UserResponse;
import com.wasil.ShopSphere.exceptions.DuplicateResourceException;
import com.wasil.ShopSphere.exceptions.InvalidPasswordException;
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
            throw new DuplicateResourceException("User with this email already exists");
        }
        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setUserEmail(userRequest.getUserEmail());
        user.setUserPassword(passwordEncoder.encode(userRequest.getUserPassword()));
        user.setUserPhone(userRequest.getUserPhone());
        user.setRole(Role.CUSTOMER);
        user.setIsActive(true);
        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    public UserResponse updateUser(String email, UpdateUserRequest request){
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUserPhone(request.getUserPhone());
        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    public void changePassword(
            String email,
            ChangePasswordRequest request) {

        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with this email"
                        ));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getUserPassword())) {

            throw new InvalidPasswordException(
                    "Current password is incorrect"
            );
        }

        user.setUserPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);
    }

    public UserResponse getUserByEmail(String email){
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return convertToResponse(user);
    }


    public UserResponse deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        if (!user.getIsActive()) {
            throw new IllegalStateException(
                    "User is already inactive"
            );
        }
        user.setIsActive(false);
        userRepository.save(user);
        return convertToResponse(user);
    }

    public UserResponse activateUser(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        if (user.getIsActive()) {
            throw new IllegalStateException(
                    "User is already active"
            );
        }
        user.setIsActive(true);
        userRepository.save(user);
        return convertToResponse(user);
    }

    private UserResponse convertToResponse(User user){
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setUserEmail(user.getUserEmail());
        response.setUserPhone(user.getUserPhone());
        response.setRole(user.getRole());
        response.setUserCreatedAt(user.getUserCreatedAt());
        response.setUserUpdatedAt(user.getUserUpdatedAt());
        response.setIsActive(user.getIsActive());
        return response;
    }
}
