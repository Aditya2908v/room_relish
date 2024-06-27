package org.example.roomrelish.services.booking;

import org.example.roomrelish.dto.BookingDetailsDTO;
import org.example.roomrelish.models.Booking;

public interface BookingInterface  {
    Booking bookRoom(BookingDetailsDTO bookingDetailsDTO);


}
