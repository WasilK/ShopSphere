package com.wasil.ShopSphere.dto.user;

import com.wasil.ShopSphere.model.AddressType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse {
    private Long addressId;
    private String street;
    private String area;
    private String city;
    private String state;
    private String zip;
    private AddressType addressType;
    private boolean defaultAddress;
}
