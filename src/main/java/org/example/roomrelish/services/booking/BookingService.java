package org.example.roomrelish.services.booking;

import lombok.RequiredArgsConstructor;
import org.example.roomrelish.constants.ApplicationConstants;
import org.example.roomrelish.dto.BookingDetailsDTO;
import org.example.roomrelish.exception.ResourceNotFoundException;
import org.example.roomrelish.exception.RoomUnavailableException;
import org.example.roomrelish.models.*;
import org.example.roomrelish.repository.BookingRepository;
import org.example.roomrelish.repository.CustomerRepository;
import org.example.roomrelish.repository.HotelRepository;
import org.example.roomrelish.repository.PaymentRepository;
import org.example.roomrelish.services.email.EmailService;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class BookingService implements BookingInterface {
    Logger logger = Logger.getLogger(getClass().getName());


    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;

    public Booking bookRoom(BookingDetailsDTO bookingDetailsDTO)  {
        if (bookingDetailsDTO == null)
            throw new IllegalArgumentException("No details provided");

        Hotel hotel = hotelRepository.findById(bookingDetailsDTO.get_hotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "hotel id", bookingDetailsDTO.get_hotelId()));
        Customer customer = customerRepository.findById(bookingDetailsDTO.get_userId())
                .orElseThrow(()-> new ResourceNotFoundException("Customer", "customer id", bookingDetailsDTO.get_userId()));

        Room requiredRoom = hotel.getRooms().stream()
                .filter(room -> room.getId().equals(bookingDetailsDTO.get_roomId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Room", "room id", bookingDetailsDTO.get_roomId()));

        validateRoomAvailability(requiredRoom.getRoomCount(), bookingDetailsDTO.getCustomerRoomCount());

        updateCustomerRecentVisits(customer, hotel);

        Booking booking = createBooking(bookingDetailsDTO, requiredRoom);

        Booking savedBooking = bookingRepository.save(booking);

        Payment payment = createPayment(savedBooking, requiredRoom, hotel);
        paymentRepository.save(payment);
        //send booking confirmation email to customer
        sendBookingConfirmationEmail(customer,savedBooking,hotel, requiredRoom);
        return savedBooking;
    }

    public Payment createPayment(Booking booking, Room requiredRoom, Hotel hotel) {
        return Payment.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .hotelId(booking.getHotelId())
                .roomId(booking.getRoomId())
                .hotelName(hotel.getHotelName())
                .roomName(requiredRoom.getRoomType())
                .numOfDays(booking.getNumOfDays())
                .numOfRooms(booking.getNumOfRooms())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .totalAmount(booking.getTotalAmount() + booking.getGstOfTotalAmount())
                .hotelImage(hotel.getImages().getFirst())
                .paymentStatus(false)
                .build();
    }

    public Booking createBooking(BookingDetailsDTO bookingDetailsDTO, Room requiredRoom) {
        double totalAmount = bookingDetailsDTO.getCustomerRoomCount() * (double) bookingDetailsDTO.getCustomerDayCount() * requiredRoom.getRoomRate();
        double totalBill = totalAmount + totalAmount * ApplicationConstants.DEFAULT_GST_PERCENTAGE;
        return Booking.builder()
                .userId(bookingDetailsDTO.get_userId())
                .hotelId(bookingDetailsDTO.get_hotelId())
                .roomId(bookingDetailsDTO.get_roomId())
                .numOfRooms(bookingDetailsDTO.getCustomerRoomCount())
                .numOfDays(bookingDetailsDTO.getCustomerDayCount())
                .checkInDate(bookingDetailsDTO.getCheckInDate())
                .checkOutDate(bookingDetailsDTO.getCheckOutDate())
                .totalAmount(totalAmount)
                .gstOfTotalAmount(totalBill)
                .build();
    }

    public void validateRoomAvailability(int roomCountBasic, int customerRoomCount) {
        logger.info("Validating whether room is available or not");
        if (roomCountBasic < customerRoomCount)
            throw new RoomUnavailableException("No available rooms");
    }

    public void updateCustomerRecentVisits(Customer customer, Hotel hotel) {
        logger.info("Updating the booking in Customer's recent visit list");
        List<String> recentVisits = customer.getRecentVisitsOfHotels();
        if (recentVisits == null) recentVisits = new ArrayList<>();
        if (!recentVisits.contains(hotel.getId())) {
            recentVisits.add(hotel.getId());
            customer.setRecentVisitsOfHotels(recentVisits);
            customerRepository.save(customer);
        }
    }

    public void sendBookingConfirmationEmail(Customer customer,Booking booking, Hotel hotel, Room room){
        String to = customer.getEmail();
        String subject = "Booking Confirmation - " + hotel.getHotelName();
        String body = generateBookingConfirmationBody(customer, booking, hotel, room);
        emailService.sendHtmlEmail(to, subject, body);
        logger.info("Booking confirmation mail sent");
    }

    public String generateBookingConfirmationBody(Customer customer, Booking booking, Hotel hotel, Room room) {

        NumberFormat formatter = new DecimalFormat("#0.00");

        return "<html><body>" +
                "<h2>Booking Confirmation - " + hotel.getHotelName() + "</h2>" +
                "<p>Dear " + customer.getUserName() + ",</p>" +
                "<p>Your booking at " + hotel.getHotelName() + " has been confirmed.</p>" +
                "<h3>Booking Details:</h3>" +
                "<ul>" +
                "<li><strong>Booking ID:</strong> " + booking.getId() + "</li>" +
                "<li><strong>Hotel Name:</strong> " + hotel.getHotelName() + "</li>" +
                "<li><strong>Room Type:</strong> " + room.getRoomType() + "</li>" +
                "<li><strong>Check-in Date:</strong> " + booking.getCheckInDate() + "</li>" +
                "<li><strong>Check-out Date:</strong> " + booking.getCheckOutDate() + "</li>" +
                "<li><strong>Total Amount:</strong> $" + formatter.format(booking.getTotalAmount()) + "</li>" +
                "</ul>" +
                "<p>Thank you for choosing " + hotel.getHotelName() + ".</p>" +
                "<p>Best regards,<br/>Hotel Management Team</p>" +
                "</body></html>";
    }
}
