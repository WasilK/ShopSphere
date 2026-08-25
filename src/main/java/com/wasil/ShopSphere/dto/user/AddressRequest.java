package com.wasil.ShopSphere.dto.user;

import com.wasil.ShopSphere.model.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank
    @Size(min = 5, max = 80)
    private String street;
    @NotBlank
    @Size(min = 5, max = 50)
    private String area;
    @NotBlank
    @Pattern(
            regexp = "^[a-zA-Z]+$",
            message = "City must contain only letters"
    )
    private String city;
    @NotBlank
    @Pattern(regexp = "^[A-Za-z]+(?: [A-Za-z]+)*$",
            message = "State can contain only letters and single spaces")
    private String state;
    @NotBlank(message = "Phone number is mandatory")
    @Pattern(
            regexp = "^[1-9][0-9]{5}$",
            message = "Zip code must contain only 6 digits"
    )
    private String zip;
    @NotNull(message = "Address type is required")
    @NotBlank
    private AddressType addressType;
    private Boolean defaultAddress;
}
