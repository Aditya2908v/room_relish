package org.example.roomrelish.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.TestOnly;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "LoginRequest",
        description = "Schema to hold login request details"
)
public class LoginRequest {

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
}
