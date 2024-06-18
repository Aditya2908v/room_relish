package org.example.roomrelish.services;

import lombok.RequiredArgsConstructor;
import org.example.roomrelish.ExceptionHandler.CustomNoBookingFoundException;
import org.example.roomrelish.ExceptionHandler.CustomNoHotelFoundException;
import org.example.roomrelish.ExceptionHandler.CustomNoPaymentFoundException;
import org.example.roomrelish.ExceptionHandler.CustomNoRoomFoundException;
import org.example.roomrelish.models.*;
import org.example.roomrelish.repository.BookingRepository;
import org.example.roomrelish.repository.HotelRepository;
import org.example.roomrelish.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final HotelRepository hotelRepository;

    @Override
    public Payment confirmBook(String bookingId) throws CustomNoRoomFoundException, CustomNoBookingFoundException, CustomNoHotelFoundException, CustomNoPaymentFoundException {
        // Setting payment status to true
        Payment currentPayment = setPaymentStatus(bookingId);
        //Modification of Room availability
        Optional<Hotel> hotelOptional = hotelRepository.findById(currentPayment.getHotelId());

        if (hotelOptional.isEmpty()) {
            throw new CustomNoHotelFoundException("No hotel found");
        }
        Hotel currentHotel = hotelOptional.get();
        Room currentRoom = currentHotel.getRooms().stream()
                .filter(room -> room.getId().equals(currentPayment.getRoomId()))
                .findFirst().orElseThrow(() -> new CustomNoRoomFoundException("Room not found"));

        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            throw new CustomNoBookingFoundException("No booking found");
        }
        Booking currentBooking = booking.get();

        modifyRoomCountForConfirmBooking(currentRoom, currentBooking, currentHotel);

        return saveBookingAndPayment(currentHotel, currentPayment);
    }

    public Payment setPaymentStatus(String bookingId) throws CustomNoPaymentFoundException {
        Optional<Payment> paymentOptional = paymentRepository.findByBookingId(bookingId);
        if (paymentOptional.isEmpty()) {
            throw new CustomNoPaymentFoundException("No payment details found");
        }
        Payment currentPayment = paymentOptional.get();
        currentPayment.setPaymentStatus(true);
        return currentPayment;
    }

    public Payment saveBookingAndPayment(Hotel currentHotel, Payment currentPayment) {
        hotelRepository.save(currentHotel);
        return paymentRepository.save(currentPayment);
    }

    public void modifyRoomCountForConfirmBooking(Room currentRoom, Booking currentBooking, Hotel currentHotel) {
        List<RoomAvailability> roomAvailabilityList = currentRoom.getRoomAvailabilityList();

        if (roomAvailabilityList == null) {
            roomAvailabilityList = new ArrayList<>();
            currentRoom.setRoomAvailabilityList(roomAvailabilityList);
        }
        RoomAvailability roomAvailability = new RoomAvailability();
        roomAvailability.setBookingId(currentBooking.getId());
        roomAvailability.setCheckInDate(currentBooking.getCheckInDate());
        roomAvailability.setCheckOutDate(currentBooking.getCheckOutDate());
        roomAvailability.setRoomCount(currentBooking.getNumOfRooms());
        currentRoom.getRoomAvailabilityList().add(roomAvailability);
        currentHotel.setRooms(currentHotel.getRooms());
    }

    @Override
    public List<Payment> getMyBookings(String userId) {
        return paymentRepository.findAllBy_userId(userId);
    }

    @Override
    public String deleteBooking(String bookingId) throws CustomNoBookingFoundException, CustomNoHotelFoundException, CustomNoRoomFoundException {
        double chargesAmount = 0.0;
        Optional<Payment> payment = paymentRepository.findByBookingId(bookingId);
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (payment.isEmpty() || booking.isEmpty()) {
            throw new CustomNoBookingFoundException("No booking found");
        }
        Payment currentPayment = payment.get();
        Booking currentBooking = booking.get();
        if (currentPayment.isPaymentStatus()) {
            chargesAmount = paymentStatusTrue(currentPayment,currentBooking,chargesAmount);
            return "Cancelled booking and the amount refunded will be " + (currentBooking.getTotalAmount() - chargesAmount);
        } else {
            deleteBookingAndPayment(currentBooking, currentPayment);
            return "Booking details deleted";
        }
    }

    public double paymentStatusTrue(Payment currentPayment, Booking currentBooking, Double chargesAmount) throws CustomNoHotelFoundException, CustomNoRoomFoundException {
        Optional<Hotel> hotel = hotelRepository.findById(currentPayment.getHotelId());
        Hotel currentHotel = hotel.orElseThrow(() -> new CustomNoHotelFoundException("Hotel not found"));

        Room currentRoom = currentHotel.getRooms().stream()
                .filter(room -> room.getId().equals(currentPayment.getRoomId()))
                .findFirst().orElseThrow(() -> new CustomNoRoomFoundException("No room found"));
        modifyRoomCountForDeleteBooking(currentBooking, currentRoom, currentHotel);
        hotelRepository.save(currentHotel);
        deleteBookingAndPayment(currentBooking, currentPayment);

        LocalDate checkInDate = currentBooking.getCheckInDate();
        LocalDate todayDate = LocalDate.now();
        int dayDifference = checkInDate.getDayOfMonth() - todayDate.getDayOfMonth();
        if (dayDifference == 0) {
            chargesAmount = currentBooking.getTotalAmount();
        } else if (dayDifference == 1) {
            chargesAmount = (50.0 / 100.0) * currentBooking.getTotalAmount();
        }
        return chargesAmount;
    }

    private void deleteBookingAndPayment(Booking currentBooking, Payment currentPayment) {
        bookingRepository.delete(currentBooking);
        paymentRepository.delete(currentPayment);
    }

    public void modifyRoomCountForDeleteBooking(Booking currentBooking, Room currentRoom, Hotel currentHotel) {
        List<RoomAvailability> availabilityList = currentRoom.getRoomAvailabilityList();
        availabilityList.removeIf(availability -> (currentBooking.getId()).equals(availability.getBookingId()));
        currentRoom.setRoomAvailabilityList(availabilityList);
        currentHotel.setRooms(currentHotel.getRooms());
    }
}
