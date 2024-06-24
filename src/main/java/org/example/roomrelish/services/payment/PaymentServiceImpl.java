package org.example.roomrelish.services.payment;

import lombok.RequiredArgsConstructor;
import org.example.roomrelish.exception.ResourceNotFoundException;
import org.example.roomrelish.models.*;
import org.example.roomrelish.repository.BookingRepository;
import org.example.roomrelish.repository.CustomerRepository;
import org.example.roomrelish.repository.HotelRepository;
import org.example.roomrelish.repository.PaymentRepository;
import org.example.roomrelish.services.email.EmailService;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
    private final EmailService emailService;
    private final CustomerRepository customerRepository;
    

    @Override
    public Payment confirmBook(String bookingId) {
        // Setting payment status to true
        Payment currentPayment = setPaymentStatus(bookingId);
        //Modification of Room availability
        Hotel currentHotel = hotelRepository.findById(currentPayment.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "hotel id", currentPayment.getHotelId()));

        Room currentRoom = currentHotel.getRooms().stream()
                .filter(room -> room.getId().equals(currentPayment.getRoomId()))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Room", "room id", currentPayment.getRoomId()));

        Booking currentBooking = bookingRepository.findById(currentPayment.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "booking id", currentPayment.getBookingId()));
        Customer customer = customerRepository.findById(currentPayment.getUserId())
                .orElseThrow(()-> new ResourceNotFoundException("Customer", "customer id", currentPayment.getUserId()));


        modifyRoomCountForConfirmBooking(currentRoom, currentBooking, currentHotel);
        sendPaymentConfirmationEmail(customer,currentBooking,currentHotel,currentPayment);

        return saveBookingAndPayment(currentHotel, currentPayment);
    }

    public Payment setPaymentStatus(String bookingId)  {
        Payment currentPayment = paymentRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "booking id", bookingId));

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
        return paymentRepository.findAllByUserId(userId);
    }

    @Override
    public String deleteBooking(String bookingId){
        double chargesAmount = 0.0;
        Payment currentPayment = paymentRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "booking id", bookingId));

        Booking currentBooking = bookingRepository.findById(currentPayment.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "booking id", currentPayment.getBookingId()));

        if (currentPayment.isPaymentStatus()) {
            chargesAmount = paymentStatusTrue(currentPayment,currentBooking,chargesAmount);
            return "Cancelled booking and the amount refunded will be " + (currentBooking.getTotalAmount() - chargesAmount);
        } else {
            deleteBookingAndPayment(currentBooking, currentPayment);
            return "Booking details deleted";
        }
    }

    public double paymentStatusTrue(Payment currentPayment, Booking currentBooking, Double chargesAmount)  {
        Hotel currentHotel = hotelRepository.findById(currentPayment.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "hotel id", currentPayment.getHotelId()));

        Room currentRoom = currentHotel.getRooms().stream()
                .filter(room -> room.getId().equals(currentPayment.getRoomId()))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Room", "room id", currentPayment.getRoomId()));
        modifyRoomCountForDeleteBooking(currentBooking, currentRoom, currentHotel);
        hotelRepository.save(currentHotel);
        deleteBookingAndPayment(currentBooking, currentPayment);

        LocalDate checkInDate = currentBooking.getCheckInDate();
        LocalDate todayDate = LocalDate.now();
        int dayDifference = checkInDate.getDayOfMonth() - todayDate.getDayOfMonth();
        if (dayDifference == 0) {
            chargesAmount = currentBooking.getTotalAmount();
        } else if (dayDifference == 1) {
            chargesAmount =  ApplicationConstants.DEFAULT_CANCELLATION_CHARGE * currentBooking.getTotalAmount();
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
    private void sendPaymentConfirmationEmail(Customer customer,Booking booking, Hotel hotel, Payment payment){
        String to = customer.getEmail();
        String subject = "Booking Confirmation - " + hotel.getHotelName();
        String body = generatePaymentConfirmationBody(customer, booking, hotel, payment);
        emailService.sendHtmlEmail(to, subject, body);
    }
    private String generatePaymentConfirmationBody(Customer customer, Booking booking, Hotel hotel, Payment payment) {

        NumberFormat formatter = new DecimalFormat("#0.00");

        return "<html><body>" +
                "<h2>Booking Confirmation - " + hotel.getHotelName() + "</h2>" +
                "<p>Dear " + customer.getUserName() + ",</p>" +
                "<p>Your have paid for your latest booking at " + hotel.getHotelName() + ".</p>" +
                "<h3>Payment Details:</h3>"+
                "<ul>"+
                "<li><strong>Payment ID:</strong>"+payment.getId()+"</li>"+
                "<li><strong>Customer Name:</strong>"+customer.getUserName()+"</li?>"+
                "<li><strong>Total Amount paid (Inclusive GST):</strong>"+payment.getTotalAmount()+"</li>"+
                "<h3>Booking Details:</h3>" +
                "<ul>" +
                "<li><strong>Booking ID:</strong> " + booking.getId() + "</li>" +
                "<li><strong>Hotel Name:</strong> " + hotel.getHotelName() + "</li>" +
                "<li><strong>Check-in Date:</strong> " + booking.getCheckInDate() + "</li>" +
                "<li><strong>Check-out Date:</strong> " + booking.getCheckOutDate() + "</li>" +
                "<li><strong>Total Amount:</strong> $" + formatter.format(booking.getTotalAmount()) + "</li>" +
                "</ul>" +
                "<p>Thank you for choosing " + hotel.getHotelName() + ".</p>" +
                "<p>Best regards,<br/>Hotel Management Team</p>" +
                "</body></html>";
    }
}
