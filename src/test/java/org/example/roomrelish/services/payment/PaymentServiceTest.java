package org.example.roomrelish.services.payment;

import com.flextrade.jfixture.FixtureAnnotations;
import com.flextrade.jfixture.annotations.Fixture;
import org.example.roomrelish.models.*;
import org.example.roomrelish.repository.*;
import org.example.roomrelish.services.email.EmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

 class PaymentServiceTest {

    @Mock
    HotelRepository hotelRepository;
    @Mock
     RoomRepository roomRepository;
    @Mock
     CustomerRepository customerRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
     EmailService emailService;
     @Fixture
     Hotel hotel;

     @Fixture
     Booking currentBooking;

     @Fixture
     Room currentRoom;

     @Fixture
     Customer customer;

     @Fixture
     List<RoomAvailability> roomAvailabilityList;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    public void setUp()
    {
        FixtureAnnotations.initFixtures(this);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConfirmBook_Success(){
        String bookingId="123";
        Payment payment = createPayment(bookingId);
        Hotel hotel = createHotel(payment.getHotelId());
        customer.setId(payment.getUserId());
        when(hotelRepository.findById(any())).thenReturn(Optional.of(hotel));
        when(roomRepository.findById(any())).thenReturn(Optional.of(createRoom(payment.getRoomId())));
        when(bookingRepository.findById(any())).thenReturn(Optional.of((createBooking(bookingId))));
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.of(payment));
        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(paymentService.confirmBook(bookingId)).thenReturn(payment);


        Payment actualPayment = paymentService.confirmBook(bookingId);

        Assertions.assertTrue(actualPayment.isPaymentStatus());

    }



     @Test
     void testSetPaymentStatus_Success() {
         String bookingId = "123";
         Payment expectedPayment = createPayment(bookingId);
         when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.of(expectedPayment));

         Payment actualPayment = paymentService.setPaymentStatus(bookingId);

         Assertions.assertEquals(expectedPayment.getId(), actualPayment.getId());
         Assertions.assertEquals(bookingId, actualPayment.getBookingId());
         Assertions.assertTrue(actualPayment.isPaymentStatus());
     }
     @Test
     void testModifyRoomCountForConfirmBooking() {

         paymentService.modifyRoomCountForConfirmBooking(currentRoom, currentBooking, hotel);

         List<RoomAvailability> availabilityList = currentRoom.getRoomAvailabilityList();
         Assertions.assertNotNull(availabilityList);
     }


     @Test
    void testSaveBookingAndPayment_Success(){
        when(paymentService.saveBookingAndPayment(hotel,createPayment("123"))).thenReturn(createPayment("123"));

        Payment payment = paymentService.saveBookingAndPayment(hotel,createPayment("123"));

        Assertions.assertNotNull(payment);
    }

    @Test
    void testDeleteBooking_Success(){
        String bookingId = "123";
        Payment payment = createPayment(bookingId);
        hotel.setId(payment.getHotelId());
        hotel.getRooms().add(createRoom("345"));

        when(paymentRepository.findByBookingId(any())).thenReturn(Optional.of(payment));
        when(bookingRepository.findById(any())).thenReturn(Optional.of(createBooking(bookingId)));
        when(hotelRepository.findById(any())).thenReturn(Optional.of(hotel));

        String result = paymentService.deleteBooking(bookingId);

        Assertions.assertNotNull(result);

    }


    @Test
    void testGetMyBooking_Success(){
        List<Payment> paymentList = new ArrayList<>();
        paymentList.add(createPayment("123"));
        paymentList.add(createPayment("123"));

        when(paymentService.getMyBookings("123")).thenReturn(paymentList);

        List<Payment> paymentList1 = paymentService.getMyBookings("123");

        Assertions.assertNotNull(paymentList1);
    }

     @Test
      void testModifyRoomCountForDeleteBooking() {
        List<RoomAvailability> availabilityList = currentRoom.getRoomAvailabilityList();
         if (availabilityList == null) {
             availabilityList = new ArrayList<>();
             currentRoom.setRoomAvailabilityList(availabilityList);
         }
         RoomAvailability roomAvailability = new RoomAvailability();
         roomAvailability.setBookingId(currentBooking.getId());
         availabilityList.add(roomAvailability);

         paymentService.modifyRoomCountForDeleteBooking(currentBooking, currentRoom, hotel);

         Assertions.assertFalse(currentRoom.getRoomAvailabilityList().stream().anyMatch(a -> a.getBookingId().equals(currentBooking.getId())));
     }


    private Payment createPayment(String bookingId){
        return Payment.builder()
                .id("123")
                .bookingId(bookingId)
                .hotelId("234")
                .roomId("345")
                .userId("123")
                .numOfRooms(1)
                .numOfDays(1)
                .totalAmount(1100.0)
                .checkInDate(createDate(27))
                .checkOutDate(createDate(28))
                .paymentStatus(true)
                .build();
    }
    private LocalDate createDate(int day){
        LocalDate currentDate = LocalDate.now();
        return LocalDate.of(currentDate.getYear(),currentDate.getMonth(),day);
    }
     private Hotel createHotel(String hotelId) {
         Hotel hotel = new Hotel();
         hotel.setId(hotelId);
         hotel.setHotelName("Sample Hotel");
         hotel.setHotelType("Luxury");
         hotel.setLocation(new Location());
         hotel.setPriceStartingFrom(200);
         hotel.setOverview("This is a sample hotel");
         hotel.setLocationFeatures(List.of("Nearby attractions", "City center location"));
         hotel.setAmenities(List.of("Free WiFi", "Swimming pool"));
         hotel.setImages(List.of("image1.jpg", "image2.jpg"));
         hotel.setRooms(List.of(createRoom("345"),createRoom("456")));
         return hotel;
     }

     private Room createRoom(String roomId) {
         return Room.builder()
                 .id(roomId)
                 .roomAvailabilityList(roomAvailabilityList)
                 .roomType("Deluxe")
                 .roomSpecification("King size")
                 .roomRate(1200)
                 .roomCount(3).build();
     }
     private Booking createBooking(String bookingId) {
         return Booking.builder()
                 .id("987")
                 .userId("123")
                 .hotelId("234")
                 .roomId("345")
                 .numOfRooms(1)
                 .numOfDays(1)
                 .totalAmount(1100.0)
                 .gstOfTotalAmount(200.0)
                 .checkInDate(createDate(27))
                 .checkOutDate(createDate(28))
                 .build();
     }

}
