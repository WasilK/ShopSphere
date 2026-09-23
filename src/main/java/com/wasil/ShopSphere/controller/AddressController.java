package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.user.AddressRequest;
import com.wasil.ShopSphere.dto.user.AddressResponse;
import com.wasil.ShopSphere.services.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "Address API's")
@RestController
public class AddressController {
    private final AddressService addressService;;
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }
    @Operation(summary = "Creates the address of logged in user.")
    @PostMapping("/users/me/address")
    public AddressResponse createAddress(Authentication authentication, @Valid @RequestBody AddressRequest addressRequest){
        String email = authentication.getName();
        return addressService.createAddress(email, addressRequest);
    }
    @Operation(summary = "Gets all the addresses of the user.")
    @GetMapping("/users/me/address")
    public List<AddressResponse> getAllAddresses(Authentication authentication){
        String email = authentication.getName();
        return addressService.getAllAddresses(email);
    }
    @Operation(summary = "Deletes the address of a user using address id.")
    @DeleteMapping("/users/me/address/{id}")
    public void deleteAddress(Authentication authentication, @PathVariable Long id){
        String email = authentication.getName();
        addressService.deleteAddress(email, id);
    }
    @Operation(summary = "Updates the address of a user using address id.")
    @PutMapping("/users/me/address/{id}")
    public AddressResponse updateAddress(Authentication authentication, @Valid @RequestBody AddressRequest addressRequest, @PathVariable Long id){
        String email = authentication.getName();
        return addressService.updateAddress(email, addressRequest, id);
    }
    @Operation(summary = "Gets a single address of a user using address id.")
    @GetMapping("/users/me/address/{id}")
    public AddressResponse getAddress(Authentication authentication, @PathVariable Long id){
        String email = authentication.getName();
        return addressService.getAddress(email, id);
    }
    @Operation(summary = "For admin to get all the addresses of all the users.")
    @GetMapping("/address")
    public List<AddressResponse> getAllAddresses(){
        return addressService.getAllAddresses();
    }
    @Operation(summary = "For admin to get a particular address of a particular user using address id.")
    @GetMapping("/address/{id}")
    public AddressResponse getAddressById(@PathVariable Long id){
        return addressService.getAddressById(id);
    }
}
