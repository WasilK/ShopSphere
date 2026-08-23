package com.wasil.ShopSphere.repositories;

import com.wasil.ShopSphere.model.Address;
import com.wasil.ShopSphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
    Optional<Address> findByAddressIdAndUser(Long addressId, User user);
}
