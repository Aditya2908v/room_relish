
package org.example.roomrelish.controllers;

import org.example.roomrelish.dto.HotelDTO;
import org.example.roomrelish.dto.ReviewDTO;
import org.example.roomrelish.dto.RoomDTO;
import org.example.roomrelish.models.GuestReview;
import org.example.roomrelish.models.Hotel;
import org.example.roomrelish.models.Room;
import org.example.roomrelish.services.HotelService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class HotelControllerTest {

    @Mock
    private HotelService hotelService;

    @InjectMocks
    private HotelController hotelController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSearchHotels_Success(){
        //Arrange
        List<Hotel> mockHotels = new ArrayList<>();
        mockHotels.add(new Hotel());
        mockHotels.add(new Hotel());

        //Act
        when(hotelService.findHotels("Chennai", 4)).thenReturn(mockHotels);
        ResponseEntity<?> response = hotelController.searchHotels("Chennai",4);

        //Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockHotels, response.getBody());
    }

    @Test
    public void testSearchHotels_NoContent(){
        when(hotelService.findHotels("NonexistentCity", null)).thenReturn(new ArrayList<>());

        ResponseEntity<?> response = hotelController.searchHotels("Nonexistent",null);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    public void testSearchHotels_Exception(){
        when(hotelService.findHotels(null, 4)).thenThrow(new IllegalArgumentException("Invalid City name"));
        ResponseEntity<?> response = hotelController.searchHotels(null,4);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(response.getBody(),"An error occurred while processing the search.");
    }

    @Test
    public void testCreateHotel_Success(){
        Hotel hotel = new Hotel();
        HotelDTO hotelDTO = new HotelDTO();
        when(hotelService.createHotel(hotelDTO)).thenReturn(hotel);

        ResponseEntity<?> response = hotelController.creteHotel(hotelDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(hotel, response.getBody());
    }

    @Test
    public void testCreateHotel_Exception(){
        when(hotelService.createHotel(null)).thenThrow(new IllegalArgumentException("Invalid City name"));
        ResponseEntity<?> response = hotelController.creteHotel(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    public void testUpdateHotel_Success(){
        Hotel hotel = new Hotel();
        HotelDTO hotelDTO = new HotelDTO();
        when(hotelService.updateHotel("1",hotelDTO)).thenReturn(hotel);
        ResponseEntity<?> response = hotelController.updateHotel("1",hotelDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(hotel, response.getBody());
    }

    @Test
    public void testUpdateHotel_Exception(){
        Hotel hotel = new Hotel();
        HotelDTO hotelDTO = new HotelDTO();
        when(hotelService.updateHotel("1",hotelDTO)).thenThrow(new IllegalArgumentException("Invalid City name"));
        ResponseEntity<?> response = hotelController.updateHotel("1",hotelDTO);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    public void testDeleteHotel_Success(){
        Hotel hotel = new Hotel();
        doNothing().when(hotelService).deleteHotel("1");
        ResponseEntity<?> response = hotelController.deleteHotel("1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Hotel deleted successfully", response.getBody());
    }

    @Test
    public void testDeleteHotel_Exception(){
        doThrow(new IllegalArgumentException("Hotel not found")).when(hotelService).deleteHotel("1");
        ResponseEntity<?> response = hotelController.deleteHotel("1");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    public void testGetAllReviews_Success(){
        String hotelId = "1";
        List<GuestReview> reviews = new ArrayList<>();
        when(hotelService.getReviews(hotelId)).thenReturn(reviews);
        ResponseEntity<?>  response = hotelController.getAllReviews(hotelId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reviews, response.getBody());
    }

    @Test
    public void testGetAllReviews_Exception(){
        String hotelId = "1";
        when(hotelService.getReviews(hotelId)).thenThrow(new IllegalArgumentException("Hotel not found"));
        ResponseEntity<?>  response = hotelController.getAllReviews(hotelId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    public void testAddGuestReview_Success(){
        ReviewDTO reviewDTO = new ReviewDTO();
        when(hotelService.addReview("1",reviewDTO)).thenReturn(new GuestReview());
        ResponseEntity<?> response = hotelController.addReview("1",reviewDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testAddGuestReview_Exception(){
        when(hotelService.addReview(null, null)).thenThrow(new IllegalArgumentException("Invalid City name"));
        ResponseEntity<?>  response = hotelController.addReview(null, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid City name",response.getBody());
    }

    @Test
    public void testDeleteGuestReview_Success(){
        String hotelId = "hotelId";
        String reviewId = "reviewId";
        doNothing().when(hotelService).deleteReview(hotelId, reviewId);
        ResponseEntity<?>  response = hotelController.deleteReview(hotelId, reviewId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testDeleteGuestReview_Exception() {
        String hotelId = "hotelId";
        String reviewId = "reviewId";
        String errorMessage = "Hotel not found";

        // Mocking the behavior of hotelService.deleteReview() to throw an IllegalArgumentException
        doThrow(new IllegalArgumentException(errorMessage)).when(hotelService).deleteReview(hotelId, reviewId);

        // Calling the endpoint
        ResponseEntity<?> response = hotelController.deleteReview(hotelId, reviewId);

        // Assertions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    public void testAddRoom_Success(){
        String hotelId = "hotelId";
        RoomDTO roomDTO = new RoomDTO();
        when(hotelService.addRoom(hotelId,roomDTO)).thenReturn(new Room());
        ResponseEntity<?>  response = hotelController.addRoom(hotelId,roomDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testAddRoom_Exception(){
        String hotelId = "hotelId";
        RoomDTO roomDTO = new RoomDTO();
        when(hotelService.addRoom(hotelId,roomDTO)).thenThrow(new IllegalArgumentException("Invalid City name"));
        ResponseEntity<?>  response = hotelController.addRoom(hotelId,roomDTO);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}