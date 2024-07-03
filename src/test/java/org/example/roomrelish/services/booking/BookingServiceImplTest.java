package org.example.roomrelish.services.booking;


import com.flextrade.jfixture.FixtureAnnotations;
import com.flextrade.jfixture.annotations.Fixture;
import org.example.roomrelish.dto.BookingDetailsDTO;
import org.example.roomrelish.models.*;
import org.example.roomrelish.repository.*;
import org.example.roomrelish.services.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class BookingServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private BookingService bookingService;

    @Fixture
    BookingDetailsDTO bookingDetailsDTO;
    @Fixture
    Hotel hotel;
    @Fixture
    Customer customer;
    @Fixture
    Room room;

    @Fixture
    Payment payment;

    @Mock
    EmailService emailService;

    @BeforeEach
    public void setUp() {
        FixtureAnnotations.initFixtures(this);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBookRoom_Success(){
        bookingDetailsDTO.setCustomerRoomCount(1);
        Booking booking = createBooking(bookingDetailsDTO);

        hotel.setId(bookingDetailsDTO.get_hotelId());
        room.setId(bookingDetailsDTO.get_roomId());
        room.setRoomCount(5);
        hotel.getRooms().add(room);
        customer.setId(bookingDetailsDTO.get_userId());
        when(hotelRepository.findById(any())).thenReturn(Optional.of(hotel));
        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(roomRepository.findById(any())).thenReturn(Optional.of(room));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(paymentRepository.save(any())).thenReturn(payment);


        Booking actualBooking = bookingService.bookRoom(bookingDetailsDTO);

        assertNotNull(actualBooking);


    }

    @Test
     void testValidateRoomAvailability_success(){
        //Arrange
        int roomCount = 7;
        int customerRoomCount = 2;
        //Act
        bookingService.validateRoomAvailability(roomCount,customerRoomCount);
    }
    private Payment createPayment(){
        return Payment.builder()
                .id("123")
                .bookingId("987")
                .hotelId("234")
                .roomId("345")
                .userId("123")
                .numOfRooms(1)
                .numOfDays(1)
                .totalAmount(1100.0)
                .checkInDate(createDate(27))
                .checkOutDate(createDate(28))
                .paymentStatus(false)
                .build();
    }

    private Hotel createSampleHotel() {
        Hotel hotel = new Hotel();
        hotel.setId("1");
        hotel.setHotelName("Sample Hotel");
        hotel.setHotelType("Luxury");
        hotel.setLocation(new Location());
        hotel.setPriceStartingFrom(200);
        hotel.setOverview("This is a sample hotel");
        hotel.setLocationFeatures(List.of("Nearby attractions", "City center location"));
        hotel.setAmenities(List.of("Free WiFi", "Swimming pool"));
        hotel.setImages(List.of("image1.jpg", "image2.jpg"));
        hotel.setRooms(List.of(new Room()));
        return hotel;
    }

    private Room createRoom() {
        return Room.builder()
                .id("123")
                .roomType("Deluxe")
                .roomSpecification("King size")
                .roomRate(1200)
                .roomCount(3).build();
    }

    private Booking createBooking(BookingDetailsDTO bookingDetailsDTO) {
        return Booking.builder()
                .id("987")
                .userId(bookingDetailsDTO.get_userId())
                .hotelId(bookingDetailsDTO.get_hotelId())
                .roomId(bookingDetailsDTO.get_roomId())
                .numOfRooms(bookingDetailsDTO.getCustomerRoomCount())
                .numOfDays(bookingDetailsDTO.getCustomerDayCount())
                .totalAmount(1100.0)
                .gstOfTotalAmount(200.0)
                .checkInDate(bookingDetailsDTO.getCheckInDate())
                .checkOutDate(bookingDetailsDTO.getCheckOutDate())
                .build();
    }
    private BookingDetailsDTO createBookingDetailsDTO() {
        return BookingDetailsDTO.builder()
                ._userId("123")
                ._hotelId("234")
                ._roomId("345")
                .customerRoomCount(1)
                .customerDayCount(1)
                .checkInDate(createDate(27))
                .checkOutDate(createDate(28))
                .build();
    }


    private LocalDate createDate(int day){
        LocalDate currentDate = LocalDate.now();
        return LocalDate.of(currentDate.getYear(),currentDate.getMonth(),day);
    }

}