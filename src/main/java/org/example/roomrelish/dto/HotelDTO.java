package org.example.roomrelish.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.roomrelish.models.GuestReview;
import org.example.roomrelish.models.Location;
import org.example.roomrelish.models.Room;
import org.jetbrains.annotations.TestOnly;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "Hotel",
        description = "Schema to hold hotel details"
)
public class HotelDTO {

    @NotEmpty(message = "Hotel name cannot be null or empty")
    @Schema(
            description = "Name of the hotel",
            example = "Grand Hotel"
    )
    private String hotelName;

    @NotEmpty(message = "Hotel type cannot be null or empty")
    @Schema(
            description = "Type of the hotel",
            example = "Luxury"
    )
    private String hotelType;

    @NotNull(message = "Location cannot be null")
    @Schema(
            description = "Location of the hotel"
    )
    private Location location;

    @DecimalMin(value = "0.0", inclusive = false, message = "Rating must be greater than 0")
    @DecimalMax(value = "5.0", message = "Rating must be less than or equal to 5")
    @Schema(
            description = "Rating of the hotel",
            example = "4.5"
    )
    private double rating;

    @NotEmpty(message = "Overall review cannot be null or empty")
    @Schema(
            description = "Overall review of the hotel",
            example = "Excellent service and amenities"
    )
    private String overallReview;

    @Min(value = 0, message = "Number of reviews must be greater than or equal to 0")
    @Schema(
            description = "Number of reviews for the hotel",
            example = "200"
    )
    private int numReviews;

    @Min(value = 0, message = "Price starting from must be greater than or equal to 0")
    @Schema(
            description = "Starting price of the hotel",
            example = "100"
    )
    private int priceStartingFrom;

    @NotEmpty(message = "Overview cannot be null or empty")
    @Schema(
            description = "Overview of the hotel",
            example = "A luxurious hotel with excellent amenities and services."
    )
    private String overview;

    @NotEmpty(message = "Location features cannot be null or empty")
    @Schema(
            description = "Features of the hotel's location",
            example = "[\"Near beach\", \"Close to city center\"]"
    )
    private List<String> locationFeatures;

    @NotEmpty(message = "Amenities cannot be null or empty")
    @Schema(
            description = "Amenities provided by the hotel",
            example = "[\"Free Wi-Fi\", \"Swimming pool\"]"
    )
    private List<String> amenities;

    @NotEmpty(message = "Images cannot be null or empty")
    @Schema(
            description = "Images of the hotel",
            example = "[\"image1.jpg\", \"image2.jpg\"]"
    )
    private List<String> images;

    @NotEmpty(message = "Rooms cannot be null or empty")
    @Schema(
            description = "List of rooms in the hotel"
    )
    private List<Room> rooms;

    @Schema(
            description = "Guest reviews for the hotel"
    )
    private List<GuestReview> guestReviews;
}


