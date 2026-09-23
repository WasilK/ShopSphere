package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.user.ChangePasswordRequest;
import com.wasil.ShopSphere.dto.user.UpdateUserRequest;
import com.wasil.ShopSphere.dto.user.UserRequest;
import com.wasil.ShopSphere.dto.user.UserResponse;

import com.wasil.ShopSphere.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "User API's")
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
     public UserController(UserService userService) {
         this.userService = userService;
     }
     @Operation(summary = "Gets all the users, only allowed for admin.")
     @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.findAllUsers();
    }
    @Operation(summary = "Gets the details of the logged in user.")
    @GetMapping("/me")
    public UserResponse getMyProfile(Authentication authentication){
         String email = authentication.getName();
        return userService.getUserByEmail(email);
    }
    @Operation(summary = "Updates the details of the logged in user.")
    @PutMapping("/me")
    public UserResponse updateUser(Authentication authentication, @Valid @RequestBody UpdateUserRequest request){
         String email = authentication.getName();
         return userService.updateUser(email, request);
    }
    @Operation(summary = "Changes the password of the logged in user.")
    @PutMapping("/me/changePassword")
    public void changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request){
         String email = authentication.getName();
         userService.changePassword(email, request);
    }
    @Operation(summary = "Gets a user using user id, only allowed for admin.")
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id){
        return userService.findUserById(id);
    }
    @Operation(summary = "Adds a user from admin end.")
    @PostMapping
    public UserResponse addUser(@Valid @RequestBody UserRequest userRequest){
        return userService.addUser(userRequest);
    }
    @Operation(summary = "Deactivates a user from admin end.")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
    }
    @Operation(summary = "Activates a user from admin end.")
    @PutMapping("{id}")
    public void activateUser(@PathVariable Long id){
         userService.activateUser(id);
    }
}
