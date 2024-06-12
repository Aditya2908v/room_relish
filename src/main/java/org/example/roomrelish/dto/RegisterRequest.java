package org.example.roomrelish.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.TestOnly;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TestOnly
@Schema(
        name = "RegisterRequest",
        description = "Schema to hold registration request details"
)
public class RegisterRequest {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(
            description = "User's username",
            example = "john_doe123"
    )
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Schema(
            description = "User's email address",
            example = "user@example.com"
    )
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password should have at least 6 characters")
    @Schema(
            description = "User's password",
            example = "password"
    )
    private String password;

    @NotNull(message = "Date of birth cannot be null")
    @Past(message = "Date of birth must be in the past")
    @Schema(
            description = "User's date of birth",
            example = "1990-01-01"
    )
    private Date dateOfBirth;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$", message = "Phone number should be valid")
    @Schema(
            description = "User's phone number",
            example = "+1234567890"
    )
    private String phoneNumber;

    @NotBlank(message = "Address cannot be blank")
    @Schema(
            description = "User's address",
            example = "123 Main St, City, Country"
    )
    private String address;
}

