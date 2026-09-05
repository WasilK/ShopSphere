package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.exceptions.UserNotFoundException;
import com.wasil.ShopSphere.model.IdempotencyKey;
import com.wasil.ShopSphere.model.IdempotencyStatus;
import com.wasil.ShopSphere.model.User;
import com.wasil.ShopSphere.repositories.IdempotencyRepository;
import com.wasil.ShopSphere.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private IdempotencyRepository idempotencyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IdempotencyService idempotencyService;


    // =========================================================
    // 1. getExistingKey() - key exists
    // =========================================================

    @Test
    void shouldGetExistingKeySuccessfully() {

        // Arrange
        String email = "test@gmail.com";
        String idempotencyKey = "abc-123";

        IdempotencyKey key = new IdempotencyKey();

        when(idempotencyRepository
                .findByUser_UserEmailAndIdempotencyKey(
                        email,
                        idempotencyKey
                ))
                .thenReturn(Optional.of(key));

        // Act
        IdempotencyKey result =
                idempotencyService.getExistingKey(
                        email,
                        idempotencyKey
                );

        // Assert
        assertNotNull(result);
        assertEquals(key, result);

        verify(idempotencyRepository)
                .findByUser_UserEmailAndIdempotencyKey(
                        email,
                        idempotencyKey
                );
    }


    // =========================================================
    // 2. getExistingKey() - key doesn't exist
    // =========================================================

    @Test
    void shouldReturnNullWhenExistingKeyNotFound() {

        // Arrange
        String email = "test@gmail.com";
        String idempotencyKey = "abc-123";

        when(idempotencyRepository
                .findByUser_UserEmailAndIdempotencyKey(
                        email,
                        idempotencyKey
                ))
                .thenReturn(Optional.empty());

        // Act
        IdempotencyKey result =
                idempotencyService.getExistingKey(
                        email,
                        idempotencyKey
                );

        // Assert
        assertNull(result);

        verify(idempotencyRepository)
                .findByUser_UserEmailAndIdempotencyKey(
                        email,
                        idempotencyKey
                );
    }


    // =========================================================
    // 3. createKey() - successful
    // =========================================================

    @Test
    void shouldCreateIdempotencyKeySuccessfully() {

        // Arrange
        String email = "test@gmail.com";
        String idempotencyKey = "abc-123";

        User user = new User();
        user.setUserEmail(email);

        IdempotencyKey savedKey = new IdempotencyKey();

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(idempotencyRepository.save(any(IdempotencyKey.class)))
                .thenReturn(savedKey);

        // Act
        IdempotencyKey result =
                idempotencyService.createKey(
                        email,
                        idempotencyKey
                );

        // Assert
        assertNotNull(result);
        assertEquals(savedKey, result);

        verify(userRepository)
                .findByUserEmail(email);

        verify(idempotencyRepository)
                .save(any(IdempotencyKey.class));
    }


    // =========================================================
    // 4. createKey() - user doesn't exist
    // =========================================================

    @Test
    void shouldThrowExceptionWhenCreatingKeyForNonExistingUser() {

        // Arrange
        String email = "unknown@gmail.com";
        String idempotencyKey = "abc-123";

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> idempotencyService.createKey(
                        email,
                        idempotencyKey
                )
        );

        assertEquals(
                "User not found with this email : " + email,
                exception.getMessage()
        );

        verify(userRepository)
                .findByUserEmail(email);

        // Key must not be saved
        verify(idempotencyRepository, never())
                .save(any(IdempotencyKey.class));
    }


    // =========================================================
    // 5. markCompleted()
    // =========================================================

    @Test
    void shouldMarkKeyAsCompleted() {

        // Arrange
        IdempotencyKey key = new IdempotencyKey();

        // Act
        idempotencyService.markCompleted(key, 100L);

        // Assert
        assertEquals(
                IdempotencyStatus.COMPLETED,
                key.getStatus()
        );

        assertEquals(
                100L,
                key.getOrderId()
        );

        verify(idempotencyRepository)
                .save(key);
    }


    // =========================================================
    // 6. markFailed()
    // =========================================================

    @Test
    void shouldMarkKeyAsFailed() {

        // Arrange
        IdempotencyKey key = new IdempotencyKey();

        // Act
        idempotencyService.markFailed(key);

        // Assert
        assertEquals(
                IdempotencyStatus.FAILED,
                key.getStatus()
        );

        verify(idempotencyRepository)
                .save(key);
    }


    // =========================================================
    // 7. markProcessing()
    // =========================================================

    @Test
    void shouldMarkKeyAsProcessing() {

        // Arrange
        IdempotencyKey key = new IdempotencyKey();

        IdempotencyKey savedKey = key;

        when(idempotencyRepository.save(key))
                .thenReturn(savedKey);

        // Act
        IdempotencyKey result =
                idempotencyService.markProcessing(key);

        // Assert
        assertEquals(
                IdempotencyStatus.PROCESSING,
                key.getStatus()
        );

        assertEquals(
                savedKey,
                result
        );

        verify(idempotencyRepository)
                .save(key);
    }
}
