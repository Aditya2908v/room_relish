package org.example.roomrelish.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomAvailability {
    private String bookingId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int roomCount;
}
