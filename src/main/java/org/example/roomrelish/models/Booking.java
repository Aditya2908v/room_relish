package org.example.roomrelish.models;

import lombok.*;
import org.jetbrains.annotations.TestOnly;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@TestOnly
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bookings")
@Builder
public class Booking {
    @Id
    private String id;
    private String userId;
    private String hotelId;
    private String roomId;
    private int numOfRooms;
    private int numOfDays;
    private double totalAmount;
    private double gstOfTotalAmount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

}
