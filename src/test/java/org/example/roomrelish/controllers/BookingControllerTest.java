package org.example.roomrelish.controllers;

import com.flextrade.jfixture.FixtureAnnotations;
import com.flextrade.jfixture.annotations.Fixture;
import com.mongodb.DuplicateKeyException;

import org.example.roomrelish.dto.BookingDetailsDTO;
import org.example.roomrelish.exception.GlobalExceptionHandler;
import org.example.roomrelish.models.Booking;
import org.example.roomrelish.services.booking.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class BookingControllerTest {



    @Mock
    private BookingService bookingService;
    @Mock
    private GlobalExceptionHandler globalExceptionHandler;
    @Fixture
    BookingDetailsDTO bookingDetailsDTO;
    @Fixture
    Booking booking;
    @InjectMocks
    private BookingController bookingController;
    @BeforeEach
    public void setUp(){
        FixtureAnnotations.initFixtures(this);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBookingDetails_success() {
        //Arrange
       Booking booking1 = createBookingFixture(bookingDetailsDTO,booking);
        when(bookingService.bookRoom(bookingDetailsDTO)).thenReturn(booking);
        //Act
        ResponseEntity<?> response = bookingController.bookingDetails(bookingDetailsDTO);
        //Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(booking,response.getBody());
        verifyBookingDetails(Objects.requireNonNull(booking1),bookingDetailsDTO);
    }

    private Booking createBookingFixture(BookingDetailsDTO bookingDetailsDTO, Booking booking) {
        booking.setUserId(bookingDetailsDTO.get_userId());
        booking.setHotelId(bookingDetailsDTO.get_hotelId());
        booking.setRoomId(bookingDetailsDTO.get_roomId());
        booking.setNumOfRooms(bookingDetailsDTO.getCustomerRoomCount());
        booking.setNumOfDays(bookingDetailsDTO.getCustomerDayCount());
        booking.setCheckInDate(bookingDetailsDTO.getCheckInDate());
        booking.setCheckOutDate(bookingDetailsDTO.getCheckOutDate());
        return booking;
    }

    private void verifyBookingDetails(Booking body, BookingDetailsDTO bookingDetailsDTO) {
        assertEquals(bookingDetailsDTO.get_userId(), body.getUserId());
        assertEquals(bookingDetailsDTO.get_hotelId(), body.getHotelId());
        assertEquals(bookingDetailsDTO.get_roomId(), body.getRoomId());
        assertEquals(bookingDetailsDTO.getCustomerRoomCount(), body.getNumOfRooms());
        assertEquals(bookingDetailsDTO.getCustomerDayCount(), body.getNumOfDays());
        assertEquals(bookingDetailsDTO.getCheckInDate(), body.getCheckInDate());
        assertEquals(bookingDetailsDTO.getCheckOutDate(), body.getCheckOutDate());
    }



    @Test
    void testBookingDetails_CustomDuplicateKeyException(){
        String errorMessage = "Duplicate booking error";
        BookingDetailsDTO bookingDetailsDTO =createBookingDetailsDTO();
        try{
            ResponseEntity<?> response = bookingController.bookingDetails(bookingDetailsDTO);
        }
        catch(DuplicateKeyException e){
            assertEquals(errorMessage, e.getMessage());
        }
    }



    @Test
    void testBookingDetails_CustomDataAccessException() throws Exception {
        String errorMessage = "Duplicate booking error";
        BookingDetailsDTO bookingDetailsDTO =createBookingDetailsDTO();

        try{
            ResponseEntity<?> response = bookingController.bookingDetails(bookingDetailsDTO);
        }
        catch(DataAccessException e){
            assertEquals(errorMessage, e.getMessage());
        }
    }


    private Booking createBooking() {
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
                .checkOutDate(createDate(29))
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
                .checkOutDate(createDate(29))
                .build();
    }

    private LocalDate createDate(int day){
        LocalDate currentDate = LocalDate.now();
        return LocalDate.of(currentDate.getYear(),currentDate.getMonth(),day);
    }







}
