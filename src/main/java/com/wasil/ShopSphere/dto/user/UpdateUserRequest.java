package com.wasil.ShopSphere.dto.user;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "First name is required")
    @Size(min = 3, max = 30, message = "First name must be between 3 and 30 characters")
    @Pattern(
            regexp = "^[a-zA-Z]+(?: [A-Za-z]+)*$",
            message = "First name must contain only letters"
    )
    private String firstName;
    @NotBlank(message = "Last name is required")
    @Size(min = 3, max = 30, message = "Last name must be between 3 and 30 characters")
    @Pattern(
            regexp = "^[a-zA-Z]*$",
            message = "Last name must contain only letters"
    )
    private String lastName;
    @NotBlank(message = "Phone number is mandatory")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone number must contain exactly 10 digits"
    )
    private String userPhone;
}
