package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.exceptions.UserNotFoundException;
import com.wasil.ShopSphere.model.IdempotencyKey;
import com.wasil.ShopSphere.model.IdempotencyStatus;
import com.wasil.ShopSphere.model.User;
import com.wasil.ShopSphere.repositories.IdempotencyRepository;
import com.wasil.ShopSphere.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;
    private final UserRepository userRepository;

    public IdempotencyService(
            IdempotencyRepository idempotencyRepository,
            UserRepository userRepository) {

        this.idempotencyRepository = idempotencyRepository;
        this.userRepository = userRepository;
    }

    /*
     * Check whether this user has already used this idempotency key.
     */
    public IdempotencyKey getExistingKey(
            String email,
            String idempotencyKey) {

        return idempotencyRepository
                .findByUser_UserEmailAndIdempotencyKey(
                        email,
                        idempotencyKey
                )
                .orElse(null);
    }

    /*
     * Create a new idempotency record.
     *
     * Initial status = PROCESSING
     */
    @Transactional
    public IdempotencyKey createKey(
            String email,
            String idempotencyKey) {

        User user = userRepository
                .findByUserEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with this email : " + email
                        )
                );

        IdempotencyKey key = new IdempotencyKey();

        key.setIdempotencyKey(idempotencyKey);
        key.setUser(user);
        key.setStatus(IdempotencyStatus.PROCESSING);

        return idempotencyRepository.save(key);
    }

    /*
     * Mark the request as successfully completed
     * and store the generated order ID.
     */
    @Transactional
    public void markCompleted(
            IdempotencyKey key,
            Long orderId) {

        key.setStatus(IdempotencyStatus.COMPLETED);
        key.setOrderId(orderId);

        idempotencyRepository.save(key);
    }

    /*
     * Mark the request as failed.
     */
    @Transactional
    public void markFailed(IdempotencyKey key) {

        key.setStatus(IdempotencyStatus.FAILED);

        idempotencyRepository.save(key);
    }

    @Transactional
    public IdempotencyKey markProcessing(IdempotencyKey key) {

        key.setStatus(IdempotencyStatus.PROCESSING);

        return idempotencyRepository.save(key);
    }
}
