package org.example.roomrelish.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(
        name = "BookingDetails",
        description = "Schema to hold booking details"
)
public class BookingDetailsDTO {

    @NotEmpty(message = "User ID cannot be null or empty")
    @Schema(
            description = "User ID",
            example = "12345"
    )
    private String _userId;

    @NotEmpty(message = "Hotel ID cannot be null or empty")
    @Schema(
            description = "Hotel ID",
            example = "67890"
    )
    private String _hotelId;

    @NotEmpty(message = "Room ID cannot be null or empty")
    @Schema(
            description = "Room ID",
            example = "54321"
    )
    private String _roomId;

    @Min(value = 1, message = "Customer room count must be at least 1")
    @Schema(
            description = "Number of rooms booked by the customer",
            example = "2"
    )
    private int customerRoomCount;

    @Min(value = 1, message = "Customer day count must be at least 1")
    @Schema(
            description = "Number of days booked by the customer",
            example = "3"
    )
    private int customerDayCount;

    @NotNull(message = "Check-in date cannot be null")
    @Schema(
            description = "Check-in date",
            example = "2024-06-12"
    )
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date cannot be null")
    @Schema(
            description = "Check-out date",
            example = "2024-06-15"
    )
    private LocalDate checkOutDate;

    @NotEmpty(message = "Room type cannot be null or empty")
    @Schema(
            description = "Type of the room",
            example = "Deluxe"
    )
    private String roomType;
}


