package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.user.AddressRequest;
import com.wasil.ShopSphere.dto.user.AddressResponse;
import com.wasil.ShopSphere.services.AddressService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AddressController {
    private final AddressService addressService;;
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }
    @PostMapping("/users/me/address")
    public AddressResponse createAddress(Authentication authentication, @Valid @RequestBody AddressRequest addressRequest){
        String email = authentication.getName();
        return addressService.createAddress(email, addressRequest);
    }
    @GetMapping("/users/me/address")
    public List<AddressResponse> getAllAddresses(Authentication authentication){
        String email = authentication.getName();
        return addressService.getAllAddresses(email);
    }
    @DeleteMapping("/users/me/address/{id}")
    public void deleteAddress(Authentication authentication, @PathVariable Long id){
        String email = authentication.getName();
        addressService.deleteAddress(email, id);
    }
    @PutMapping("/users/me/address/{id}")
    public AddressResponse updateAddress(Authentication authentication, @Valid @RequestBody AddressRequest addressRequest, @PathVariable Long id){
        String email = authentication.getName();
        return addressService.updateAddress(email, addressRequest, id);
    }
    @GetMapping("/users/me/address/{id}")
    public AddressResponse getAddress(Authentication authentication, @PathVariable Long id){
        String email = authentication.getName();
        return addressService.getAddress(email, id);
    }
    @GetMapping("/address")
    public List<AddressResponse> getAllAddresses(){
        return addressService.getAllAddresses();
    }

    @GetMapping("/address/{id}")
    public AddressResponse getAddressById(@PathVariable Long id){
        return addressService.getAddressById(id);
    }
}
