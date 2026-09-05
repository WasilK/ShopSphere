package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.user.ChangePasswordRequest;
import com.wasil.ShopSphere.dto.user.UpdateUserRequest;
import com.wasil.ShopSphere.dto.user.UserRequest;
import com.wasil.ShopSphere.dto.user.UserResponse;

import com.wasil.ShopSphere.services.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
     public UserController(UserService userService) {
         this.userService = userService;
     }

     @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.findAllUsers();
    }

    @GetMapping("/me")
    public UserResponse getMyProfile(Authentication authentication){
         String email = authentication.getName();
        return userService.getUserByEmail(email);
    }
    @PutMapping("/me")
    public UserResponse updateUser(Authentication authentication, @Valid @RequestBody UpdateUserRequest request){
         String email = authentication.getName();
         return userService.updateUser(email, request);
    }
    @PutMapping("/me/changePassword")
    public void changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request){
         String email = authentication.getName();
         userService.changePassword(email, request);
    }
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id){
        return userService.findUserById(id);
    }

    @PostMapping
    public UserResponse addUser(@Valid @RequestBody UserRequest userRequest){
        return userService.addUser(userRequest);
    }


    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
    }

    @PutMapping("{id}")
    public void activateUser(@PathVariable Long id){
         userService.activateUser(id);
    }
}
