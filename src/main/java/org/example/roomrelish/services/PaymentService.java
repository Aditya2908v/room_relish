package org.example.roomrelish.services;

import org.example.roomrelish.ExceptionHandler.CustomNoBookingFoundException;
import org.example.roomrelish.ExceptionHandler.CustomNoHotelFoundException;
import org.example.roomrelish.ExceptionHandler.CustomNoPaymentFoundException;
import org.example.roomrelish.ExceptionHandler.CustomNoRoomFoundException;
import org.example.roomrelish.models.Payment;
import org.jetbrains.annotations.TestOnly;

import java.util.List;
@TestOnly
public interface PaymentService {
    Payment confirmBook(String _bookingId) throws CustomNoRoomFoundException, CustomNoBookingFoundException, CustomNoHotelFoundException, CustomNoPaymentFoundException;
    List<Payment> getMyBookings(String _userId);
    String deleteBooking(String _bookingId) throws CustomNoBookingFoundException, CustomNoHotelFoundException, CustomNoRoomFoundException;

}
