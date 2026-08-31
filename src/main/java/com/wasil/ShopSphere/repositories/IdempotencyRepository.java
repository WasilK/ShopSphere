package com.wasil.ShopSphere.repositories;

import com.wasil.ShopSphere.model.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByUser_UserEmailAndIdempotencyKey(
            String userEmail,
            String idempotencyKey
    );
}
