package org.example.roomrelish.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.TestOnly;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TestOnly
@Schema(
        name = "SearchCriteria",
        description = "Schema to hold search criteria for hotel search"
)
public class SearchCriteria {

    @Min(value = 0, message = "Minimum price cannot be negative")
    @Schema(
            description = "Minimum price criteria for hotel search"
    )
    private Integer minPrice;

    @Min(value = 0, message = "Maximum price cannot be negative")
    @Schema(
            description = "Maximum price criteria for hotel search"
    )
    private Integer maxPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Rating must be greater than 0")
    @DecimalMax(value = "5.0", message = "Rating must be less than or equal to 5")
    @Schema(
            description = "Rating criteria for hotel search"
    )
    private Double rating;

    @Schema(
            description = "List of amenities criteria for hotel search"
    )
    private List<String> amenities;
}
