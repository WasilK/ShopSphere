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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    void createUserSuccessfully(){
        UserRequest request = new UserRequest();

        request.setFirstName("Wasil");
        request.setLastName("Khan");
        request.setUserEmail("test@gmail.com");
        request.setUserPassword("password123");
        request.setUserPhone("9876543210");

        User user = new User();

        user.setUserId(1L);
        user.setFirstName("Wasil");
        user.setLastName("Khan");
        user.setUserEmail("test@gmail.com");
        user.setUserPassword("encodedPassword");
        user.setUserPhone("9876543210");
        user.setRole(Role.CUSTOMER);
        user.setIsActive(true);

        when(userRepository.findByUserEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        // Act
        UserResponse response = userService.addUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("Wasil", response.getFirstName());
        assertEquals("Khan", response.getLastName());
        assertEquals("test@gmail.com", response.getUserEmail());
        assertEquals("9876543210", response.getUserPhone());

        verify(userRepository).findByUserEmail("test@gmail.com");
        verify(passwordEncoder).encode("password123");
        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertEquals("test@gmail.com", savedUser.getUserEmail());
        assertEquals("encodedPassword", savedUser.getUserPassword());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        // Arrange
        UserRequest request = new UserRequest();

        request.setFirstName("Wasil");
        request.setLastName("Khan");
        request.setUserEmail("test@gmail.com");
        request.setUserPassword("password123");
        request.setUserPhone("9876543210");

        User existingUser = new User();

        when(userRepository.findByUserEmail("test@gmail.com"))
                .thenReturn(Optional.of(existingUser));

        // Act + Assert
        assertThrows(
                DuplicateResourceException.class,
                () -> userService.addUser(request)
        );

        // Verify
        verify(userRepository).findByUserEmail("test@gmail.com");

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void shouldFindUserByIdSuccessfully() {

        // Arrange
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setFirstName("Wasil");
        user.setLastName("Khan");
        user.setUserEmail("test@gmail.com");
        user.setUserPhone("9876543210");
        user.setIsActive(true);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.findUserById(userId);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("Wasil", response.getFirstName());
        assertEquals("Khan", response.getLastName());
        assertEquals("test@gmail.com", response.getUserEmail());
        assertEquals("9876543210", response.getUserPhone());
        assertEquals(true, response.getIsActive());

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundById() {

        // Arrange
        Long userId = 99L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act + Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.findUserById(userId)
        );

        assertEquals(
                "User not found with id: 99",
                exception.getMessage()
        );

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldUpdateUserSuccessfully() {

        // Arrange
        String email = "test@gmail.com";

        User user = new User();
        user.setUserId(1L);
        user.setFirstName("Old");
        user.setLastName("Name");
        user.setUserEmail(email);
        user.setUserPhone("1111111111");
        user.setIsActive(true);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Wasil");
        request.setLastName("Khan");
        request.setUserPhone("9876543210");

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        // Act
        UserResponse response = userService.updateUser(email, request);

        // Assert
        assertNotNull(response);
        assertEquals("Wasil", response.getFirstName());
        assertEquals("Khan", response.getLastName());
        assertEquals("9876543210", response.getUserPhone());

        verify(userRepository).findByUserEmail(email);
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingUser() {

        String email = "unknown@gmail.com";

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Wasil");
        request.setLastName("Khan");
        request.setUserPhone("9876543210");

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(email, request)
        );

        assertEquals(
                "User not found with email: " + email,
                exception.getMessage()
        );

        verify(userRepository).findByUserEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldChangePasswordSuccessfully() {

        // Arrange
        String email = "test@gmail.com";

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");

        User user = new User();
        user.setUserId(1L);
        user.setUserEmail(email);
        user.setUserPassword("encodedOldPassword");

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "oldPassword",
                "encodedOldPassword"
        )).thenReturn(true);

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encodedNewPassword");

        // Act
        userService.changePassword(email, request);

        // Assert
        assertEquals(
                "encodedNewPassword",
                user.getUserPassword()
        );

        verify(userRepository).findByUserEmail(email);

        verify(passwordEncoder).matches(
                "oldPassword",
                "encodedOldPassword"
        );

        verify(passwordEncoder).encode("newPassword");

        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenCurrentPasswordIsIncorrect() {

        // Arrange
        String email = "test@gmail.com";

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword");

        User user = new User();
        user.setUserId(1L);
        user.setUserEmail(email);
        user.setUserPassword("encodedOldPassword");

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrongPassword",
                "encodedOldPassword"
        )).thenReturn(false);

        // Act + Assert
        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> userService.changePassword(email, request)
        );

        assertEquals(
                "Current password is incorrect",
                exception.getMessage()
        );

        // New password must NOT be encoded
        verify(passwordEncoder, never())
                .encode("newPassword");

        // User must NOT be saved
        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenChangingPasswordForNonExistingUser() {

        // Arrange
        String email = "unknown@gmail.com";

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.changePassword(email, request)
        );

        assertEquals(
                "User not found with this email",
                exception.getMessage()
        );

        // Nothing related to password should happen
        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldGetUserByEmailSuccessfully() {

        // Arrange
        String email = "test@gmail.com";

        User user = new User();
        user.setUserId(1L);
        user.setFirstName("Wasil");
        user.setLastName("Khan");
        user.setUserEmail(email);
        user.setUserPhone("9876543210");
        user.setIsActive(true);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.getUserByEmail(email);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("Wasil", response.getFirstName());
        assertEquals("Khan", response.getLastName());
        assertEquals(email, response.getUserEmail());
        assertEquals("9876543210", response.getUserPhone());
        assertEquals(true, response.getIsActive());

        verify(userRepository).findByUserEmail(email);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByEmail() {

        // Arrange
        String email = "unknown@gmail.com";

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserByEmail(email)
        );

        assertEquals(
                "User not found with email: " + email,
                exception.getMessage()
        );

        verify(userRepository).findByUserEmail(email);
    }

}
