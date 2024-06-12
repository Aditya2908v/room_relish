package org.example.roomrelish.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.TestOnly;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "Authentication Response",
        description = "Schema to hold Authentication token"
)
public class AuthResponse {
    @JsonProperty("access_token")
    private String token;
    private String userId;
}
