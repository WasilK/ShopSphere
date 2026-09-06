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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {

        user = new User();

        user.setUserId(1L);
        user.setFirstName("Wasil");
        user.setLastName("Khan");
        user.setUserEmail("wasil@gmail.com");
        user.setUserPassword("encodedPassword");
        user.setUserPhone("9876543210");
        user.setRole(Role.CUSTOMER);
        user.setIsActive(true);
    }

    // =========================================================
    // findAllUsers()
    // =========================================================

    @Test
    void findAllUsers_shouldReturnUsers() {

        when(userRepository.findAll())
                .thenReturn(List.of(user));

        List<UserResponse> result = userService.findAllUsers();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals("Wasil", result.get(0).getFirstName());
        assertEquals("Khan", result.get(0).getLastName());
        assertEquals("wasil@gmail.com", result.get(0).getUserEmail());
        assertEquals("9876543210", result.get(0).getUserPhone());
        assertTrue(result.get(0).getIsActive());

        verify(userRepository).findAll();
    }

    @Test
    void findAllUsers_shouldReturnEmptyList_whenNoUsersExist() {

        when(userRepository.findAll())
                .thenReturn(List.of());

        List<UserResponse> result = userService.findAllUsers();

        assertTrue(result.isEmpty());

        verify(userRepository).findAll();
    }


    // =========================================================
    // findUserById()
    // =========================================================

    @Test
    void findUserById_shouldReturnUser() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponse response = userService.findUserById(1L);

        assertEquals(1L, response.getUserId());
        assertEquals("Wasil", response.getFirstName());
        assertEquals("wasil@gmail.com", response.getUserEmail());

        verify(userRepository).findById(1L);
    }

    @Test
    void findUserById_shouldThrowException_whenUserNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.findUserById(1L)
        );

        verify(userRepository).findById(1L);
    }


    // =========================================================
    // addUser()
    // =========================================================

    @Test
    void addUser_shouldCreateUserSuccessfully() {

        UserRequest request = new UserRequest();

        request.setFirstName("Wasil");
        request.setLastName("Khan");
        request.setUserEmail("wasil@gmail.com");
        request.setUserPassword("plainPassword");
        request.setUserPhone("9876543210");

        when(userRepository.findByUserEmail("wasil@gmail.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("plainPassword"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserResponse response = userService.addUser(request);

        assertEquals(1L, response.getUserId());
        assertEquals("Wasil", response.getFirstName());
        assertEquals("wasil@gmail.com", response.getUserEmail());

        verify(userRepository).findByUserEmail("wasil@gmail.com");
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addUser_shouldSetDefaultRoleAndActiveStatus() {

        UserRequest request = new UserRequest();

        request.setFirstName("Wasil");
        request.setLastName("Khan");
        request.setUserEmail("wasil@gmail.com");
        request.setUserPassword("plainPassword");
        request.setUserPhone("9876543210");

        when(userRepository.findByUserEmail("wasil@gmail.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("plainPassword"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        userService.addUser(request);

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertEquals("Wasil", savedUser.getFirstName());
        assertEquals("Khan", savedUser.getLastName());
        assertEquals("wasil@gmail.com", savedUser.getUserEmail());

        assertEquals("encodedPassword", savedUser.getUserPassword());

        assertEquals(Role.CUSTOMER, savedUser.getRole());
        assertTrue(savedUser.getIsActive());
    }

    @Test
    void addUser_shouldThrowException_whenEmailAlreadyExists() {

        UserRequest request = new UserRequest();

        request.setUserEmail("wasil@gmail.com");

        when(userRepository.findByUserEmail("wasil@gmail.com"))
                .thenReturn(Optional.of(user));

        assertThrows(
                DuplicateResourceException.class,
                () -> userService.addUser(request)
        );

        verify(userRepository).findByUserEmail("wasil@gmail.com");

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }


    // =========================================================
    // updateUser()
    // =========================================================

    @Test
    void updateUser_shouldUpdateSuccessfully() {

        UpdateUserRequest request = new UpdateUserRequest();

        request.setFirstName("Mohammad");
        request.setLastName("Khan");
        request.setUserPhone("9999999999");

        when(userRepository.findByUserEmail("wasil@gmail.com"))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        UserResponse response =
                userService.updateUser("wasil@gmail.com", request);

        assertEquals("Mohammad", response.getFirstName());
        assertEquals("Khan", response.getLastName());
        assertEquals("9999999999", response.getUserPhone());

        verify(userRepository).findByUserEmail("wasil@gmail.com");
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_shouldThrowException_whenUserNotFound() {

        UpdateUserRequest request = new UpdateUserRequest();

        request.setFirstName("Mohammad");
        request.setLastName("Khan");
        request.setUserPhone("9999999999");

        when(userRepository.findByUserEmail("wasil@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(
                        "wasil@gmail.com",
                        request
                )
        );

        verify(userRepository)
                .findByUserEmail("wasil@gmail.com");

        verify(userRepository, never())
                .save(any(User.class));
    }


    // =========================================================
    // changePassword()
    // =========================================================

    @Test
    void changePassword_shouldChangePasswordSuccessfully() {

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findByUserEmail("wasil@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "oldPassword",
                "encodedPassword"
        )).thenReturn(true);

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("newEncodedPassword");

        userService.changePassword(
                "wasil@gmail.com",
                request
        );

        assertEquals(
                "newEncodedPassword",
                user.getUserPassword()
        );

        verify(passwordEncoder)
                .matches(
                        "oldPassword",
                        "encodedPassword"
                );

        verify(passwordEncoder)
                .encode("newPassword");

        verify(userRepository)
                .save(user);
    }

    @Test
    void changePassword_shouldThrowException_whenUserNotFound() {

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findByUserEmail("wasil@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.changePassword(
                        "wasil@gmail.com",
                        request
                )
        );

        verify(userRepository)
                .findByUserEmail("wasil@gmail.com");

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void changePassword_shouldThrowException_whenCurrentPasswordIsWrong() {

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findByUserEmail("wasil@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrongPassword",
                "encodedPassword"
        )).thenReturn(false);

        assertThrows(
                InvalidPasswordException.class,
                () -> userService.changePassword(
                        "wasil@gmail.com",
                        request
                )
        );

        verify(passwordEncoder)
                .matches(
                        "wrongPassword",
                        "encodedPassword"
                );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }


    // =========================================================
    // getUserByEmail()
    // =========================================================

    @Test
    void getUserByEmail_shouldReturnUser() {

        when(userRepository.findByUserEmail("wasil@gmail.com"))
                .thenReturn(Optional.of(user));

        UserResponse response =
                userService.getUserByEmail("wasil@gmail.com");

        assertEquals(1L, response.getUserId());
        assertEquals("Wasil", response.getFirstName());
        assertEquals("wasil@gmail.com", response.getUserEmail());

        verify(userRepository)
                .findByUserEmail("wasil@gmail.com");
    }

    @Test
    void getUserByEmail_shouldThrowException_whenUserNotFound() {

        when(userRepository.findByUserEmail("wasil@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserByEmail(
                        "wasil@gmail.com"
                )
        );

        verify(userRepository)
                .findByUserEmail("wasil@gmail.com");
    }


    // =========================================================
    // deleteUser()
    // =========================================================

    @Test
    void deleteUser_shouldDeactivateUserSuccessfully() {

        user.setIsActive(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        UserResponse response =
                userService.deleteUser(1L);

        assertFalse(user.getIsActive());
        assertFalse(response.getIsActive());

        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_shouldThrowException_whenUserNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(1L)
        );

        verify(userRepository).findById(1L);

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void deleteUser_shouldThrowException_whenUserAlreadyInactive() {

        user.setIsActive(false);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                IllegalStateException.class,
                () -> userService.deleteUser(1L)
        );

        verify(userRepository).findById(1L);

        verify(userRepository, never())
                .save(any(User.class));
    }


    // =========================================================
    // activateUser()
    // =========================================================

    @Test
    void activateUser_shouldActivateUserSuccessfully() {

        user.setIsActive(false);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        UserResponse response =
                userService.activateUser(1L);

        assertTrue(user.getIsActive());
        assertTrue(response.getIsActive());

        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void activateUser_shouldThrowException_whenUserNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.activateUser(1L)
        );

        verify(userRepository).findById(1L);

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void activateUser_shouldThrowException_whenUserAlreadyActive() {

        user.setIsActive(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                IllegalStateException.class,
                () -> userService.activateUser(1L)
        );

        verify(userRepository).findById(1L);

        verify(userRepository, never())
                .save(any(User.class));
    }
}