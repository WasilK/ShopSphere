package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.user.AddressRequest;
import com.wasil.ShopSphere.dto.user.AddressResponse;
import com.wasil.ShopSphere.exceptions.AddressNotFoundException;
import com.wasil.ShopSphere.exceptions.UserNotFoundException;
import com.wasil.ShopSphere.model.Address;
import com.wasil.ShopSphere.model.User;
import com.wasil.ShopSphere.repositories.AddressRepository;
import com.wasil.ShopSphere.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    public AddressService(AddressRepository addressRepository, UserRepository userRepository){
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    public AddressResponse createAddress(String email, AddressRequest addressRequest){
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with this email id "));
        Address address = new Address();
        address.setStreet(addressRequest.getStreet());
        address.setArea(addressRequest.getArea());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setZip(addressRequest.getZip());
        address.setAddressType(addressRequest.getAddressType());
        address.setUser(user);

        if (user.getAddresses().isEmpty()) {
            address.setDefaultAddress(true);
        }
        // User explicitly wants this address as default
        else if (Boolean.TRUE.equals(addressRequest.getDefaultAddress())) {

            // Remove default status from old address
            user.getAddresses().forEach(existingAddress ->
                    existingAddress.setDefaultAddress(false)
            );

            address.setDefaultAddress(true);
        }
        // User doesn't want this address as default
        else {
            address.setDefaultAddress(false);
        }

        user.getAddresses().add(address);
        Address savedAddress = addressRepository.save(address);
        return convertToResponse(savedAddress);
    }
    public List<AddressResponse> getAllAddresses(String email){
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with this email."));
        List<Address> address = addressRepository.findByUser(user);
        return address.stream().map(this::convertToResponse).toList();
    }
    public AddressResponse getAddress(String email, Long id){
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with this email."));
        Address address = addressRepository.findByAddressIdAndUser(id, user).orElseThrow(() -> new AddressNotFoundException("Address not found with the given user and id"));
        return convertToResponse(address);
    }
    public void deleteAddress(String email, Long id) {

        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with this email."
                        ));

        Address address = addressRepository.findByAddressIdAndUser(id, user)
                .orElseThrow(() ->
                        new AddressNotFoundException(
                                "Address not found."
                        ));

        addressRepository.delete(address);
    }
    public AddressResponse updateAddress(String email, AddressRequest addressRequest, Long id){
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with this email."));
        Address address = addressRepository.findByAddressIdAndUser(id, user).orElseThrow(() -> new AddressNotFoundException("Address not found with this id."));
        address.setStreet(addressRequest.getStreet());
        address.setArea(addressRequest.getArea());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setZip(addressRequest.getZip());
        address.setAddressType(addressRequest.getAddressType());
        address.setUser(user);
        if (Boolean.TRUE.equals(addressRequest.getDefaultAddress())) {
            user.getAddresses().forEach(existingAddress ->
                    existingAddress.setDefaultAddress(false)
            );
            address.setDefaultAddress(true);
        }else{
            address.setDefaultAddress(false);
        }
        Address savedAddress = addressRepository.save(address);
        return convertToResponse(savedAddress);
    }
    public List<AddressResponse> getAllAddresses(){
        return addressRepository.findAll().stream().map(this::convertToResponse).toList();
    }
    public AddressResponse getAddressById(Long id){
        Address address = addressRepository.findById(id).orElseThrow(() -> new AddressNotFoundException("Address not found with the given id"));
        return convertToResponse(address);
    }
        private AddressResponse convertToResponse(Address address) {
        return new AddressResponse(
                address.getAddressId(),
                address.getStreet(),
                address.getArea(),
                address.getCity(),
                address.getState(),
                address.getZip(),
                address.getAddressType(),
                address.isDefaultAddress()
        );
    }
}
