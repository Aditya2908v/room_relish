package org.example.roomrelish.services;

import lombok.RequiredArgsConstructor;
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
    public Payment confirmBook(String _bookingId) {
        Optional<Payment> paymentOptional = paymentRepository.findBy_bookingId(_bookingId);
        if (paymentOptional.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Payment currentPayment = paymentOptional.get();
        // Setting payment status to true
        currentPayment.setPaymentStatus(true);
        Optional<Hotel> hotelOptional = hotelRepository.findById(currentPayment.get_hotelId());

        if (hotelOptional.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Hotel currentHotel = hotelOptional.get();
        Room currentRoom = currentHotel.getRooms().stream()
                .filter(room -> room.getId().equals(currentPayment.get_roomId()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Room not found"));

        Optional<Booking> booking = bookingRepository.findById(_bookingId);
        if (booking.isEmpty()) {
            throw new IllegalArgumentException("No booking found");
        }
        Booking currentBooking = booking.get();

        modifyRoomCountForConfirmBooking(currentRoom, currentBooking, currentHotel);

        return saveBookingAndPayment(currentHotel, currentPayment);
    }

    private Payment saveBookingAndPayment(Hotel currentHotel, Payment currentPayment) {
        hotelRepository.save(currentHotel);
        return paymentRepository.save(currentPayment);
    }

    private void modifyRoomCountForConfirmBooking(Room currentRoom, Booking currentBooking, Hotel currentHotel) {
        List<RoomAvailability> roomAvailabilityList = currentRoom.getRoomAvailabilityList();

        if (roomAvailabilityList == null) {
            roomAvailabilityList = new ArrayList<>();
            currentRoom.setRoomAvailabilityList(roomAvailabilityList);
        }
        RoomAvailability roomAvailability = new RoomAvailability();
        roomAvailability.set_bookingId(currentBooking.getId());
        roomAvailability.setCheckInDate(currentBooking.getCheckInDate());
        roomAvailability.setCheckOutDate(currentBooking.getCheckOutDate());
        roomAvailability.setRoomCount(currentBooking.getNumOfRooms());
        currentRoom.getRoomAvailabilityList().add(roomAvailability);
        currentHotel.setRooms(currentHotel.getRooms());
    }

    @Override
    public List<Payment> getMyBookings(String _userId) {
        return paymentRepository.findAllBy_userId(_userId);
    }

    @Override
    public String deleteBooking(String _bookingId) {
        double chargesAmount = 0.0;
        Optional<Payment> payment = paymentRepository.findBy_bookingId(_bookingId);
        Optional<Booking> booking = bookingRepository.findById(_bookingId);
        if (payment.isEmpty() || booking.isEmpty()) {
            throw new IllegalArgumentException("No booking found");
        }
        Payment currentPayment = payment.get();
        Booking currentBooking = booking.get();
        if (currentPayment.isPaymentStatus()) {
            Optional<Hotel> hotel = hotelRepository.findById(currentPayment.get_hotelId());
            Hotel currentHotel = hotel.orElseThrow(() -> new IllegalArgumentException("Hotel not found"));

            Room currentRoom = currentHotel.getRooms().stream()
                    .filter(room -> room.getId().equals(currentPayment.get_roomId()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("No room found"));
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
            return "Cancelled booking and the amount refunded will be " + (currentBooking.getTotalAmount() - chargesAmount);
        } else {
            deleteBookingAndPayment(currentBooking, currentPayment);
            return "Booking details deleted";
        }
    }

    private void deleteBookingAndPayment(Booking currentBooking, Payment currentPayment) {
        bookingRepository.delete(currentBooking);
        paymentRepository.delete(currentPayment);
    }

    private void modifyRoomCountForDeleteBooking(Booking currentBooking, Room currentRoom, Hotel currentHotel) {
        List<RoomAvailability> availabilityList = currentRoom.getRoomAvailabilityList();
        availabilityList.removeIf(availability -> (currentBooking.getId()).equals(availability.get_bookingId()));
        currentRoom.setRoomAvailabilityList(availabilityList);
        currentHotel.setRooms(currentHotel.getRooms());
    }
}
