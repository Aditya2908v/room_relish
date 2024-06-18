package org.example.roomrelish.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.TestOnly;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TestOnly
@Schema(
        name = "UpdateCustomerDTO",
        description = "Schema to hold customer update details"
)
public class UpdateCustomerDTO {

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password should have at least 6 characters")
    @Schema(
            description = "User's updated password",
            example = "newPassword"
    )
    private String password;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$", message = "Phone number should be valid")
    @Schema(
            description = "User's updated phone number",
            example = "+1234567890"
    )
    private String phoneNumber;
}

