package com.wasil.ShopSphere.dto.user;

import com.wasil.ShopSphere.model.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRequest {
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

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format")
    private String userEmail;

    @NotBlank(message = "Password is mandatory.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."
    )
    private String userPassword;

    @NotBlank(message = "Phone number is mandatory")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone number must contain exactly 10 digits"
    )
    private String userPhone;
}
