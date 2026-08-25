package com.wasil.ShopSphere.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;
    private String street;
    private String area;
    private String city;
    private String state;
    private String zip;
    @Enumerated(EnumType.STRING)
    private AddressType addressType;
    private boolean defaultAddress;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
