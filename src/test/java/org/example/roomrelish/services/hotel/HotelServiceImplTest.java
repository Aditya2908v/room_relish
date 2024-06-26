package org.example.roomrelish.services.hotel;

import org.example.roomrelish.dto.*;
import org.example.roomrelish.exception.ResourceNotFoundException;
import org.example.roomrelish.models.Customer;
import org.example.roomrelish.models.GuestReview;
import org.example.roomrelish.models.Hotel;
import org.example.roomrelish.models.Room;
import org.example.roomrelish.repository.CustomerRepository;
import org.example.roomrelish.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private Hotel testHotel;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setId("1");
        testHotel.setHotelName("Test Hotel");
        testHotel.setRating(4.5);

        testCustomer = new Customer();
        testCustomer.setId("1");
        testCustomer.setUsername("John Doe");
    }

    @Test
    public void testGetAllHotels() {
        List<Hotel> dummyHotels = new ArrayList<>();
        Hotel hotelA = new Hotel();
        hotelA.setId("1");
        hotelA.setHotelName("Hotel A");
        Hotel hotelB = new Hotel();
        hotelB.setId("2");
        hotelB.setHotelName("Hotel B");
        dummyHotels.add(hotelA);
        dummyHotels.add(hotelB);

        when(hotelRepository.findAll()).thenReturn(dummyHotels);

        List<Hotel> hotels = hotelService.getAllHotels();

        assertEquals(2, hotels.size());
        assertEquals("Hotel A", hotels.get(0).getHotelName());
        assertEquals("Hotel B", hotels.get(1).getHotelName());
    }

    @Test
     void testFindHotelById_existingId() {
        when(hotelRepository.findById("1")).thenReturn(Optional.of(testHotel));

        Hotel foundHotel = hotelService.findHotelById("1");

        assertNotNull(foundHotel);
        assertEquals("Test Hotel", foundHotel.getHotelName());
    }

    @Test
     void testFindHotelById_nonExistingId() {
        when(hotelRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> hotelService.findHotelById("999"));
    }

    @Test
     void testCreateHotel() {
        HotelDTO hotelDTO = new HotelDTO();
        hotelDTO.setHotelName("New Hotel");
        hotelDTO.setRating(4.0);

        when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> {
            Hotel savedHotel = invocation.getArgument(0);
            savedHotel.setId("2"); // Simulating saving with generated id
            return savedHotel;
        });

        hotelService.createHotel(hotelDTO);

        // Verify hotel was saved
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
     void testUpdateHotel_existingId() {
        HotelDTO hotelDTO = new HotelDTO();
        hotelDTO.setHotelName("Updated Hotel");
        hotelDTO.setRating(4.2);

        when(hotelRepository.findById("1")).thenReturn(Optional.of(testHotel));

        hotelService.updateHotel("1", hotelDTO);

        assertEquals("Updated Hotel", testHotel.getHotelName());
        assertEquals(4.2, testHotel.getRating());
    }

    @Test
     void testUpdateHotel_nonExistingId() {
        HotelDTO hotelDTO = new HotelDTO();
        hotelDTO.setHotelName("Updated Hotel");

        when(hotelRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> hotelService.updateHotel("999", hotelDTO));
    }

    @Test
    public void testDeleteHotel_existingId() {
        when(hotelRepository.findById("1")).thenReturn(Optional.of(testHotel));

        hotelService.deleteHotel("1");

        verify(hotelRepository, times(1)).delete(testHotel);
    }

    @Test
     void testDeleteHotel_nonExistingId() {
        when(hotelRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> hotelService.deleteHotel("999"));
    }

    @Test
     void testGetReviews() {
        List<GuestReview> reviews = new ArrayList<>();
        GuestReview guestReview = new GuestReview();
        guestReview.setUser("1");
        guestReview.setGuestRating(4.0);
        guestReview.setComment("Good review");
        reviews.add(guestReview);

        testHotel.setGuestReviews(reviews);

        when(hotelRepository.findById("1")).thenReturn(Optional.of(testHotel));
        when(customerRepository.findById("1")).thenReturn(Optional.of(testCustomer));

        List<ReviewResponse> reviewResponses = hotelService.getReviews("1");

        assertEquals(1, reviewResponses.size());
        assertEquals("John Doe", reviewResponses.getFirst().getCustomerName());
        assertEquals(4.0, reviewResponses.getFirst().getGuestRating());
        assertEquals("Good review", reviewResponses.getFirst().getComment());
    }

    @Test
     void testGetReviews_noCustomerFound() {
        List<GuestReview> reviews = new ArrayList<>();
        GuestReview guestReview = new GuestReview();
        guestReview.setUser("1");
        guestReview.setGuestRating(4.0);
        guestReview.setComment("Good review");
        reviews.add(guestReview);

        testHotel.setGuestReviews(reviews);

        when(hotelRepository.findById("1")).thenReturn(Optional.of(testHotel));
        when(customerRepository.findById("1")).thenReturn(Optional.empty());

        List<ReviewResponse> reviewResponses = hotelService.getReviews("1");

        assertEquals(0, reviewResponses.size());
    }

    @Test
     void testAddReview() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setUserid("1");
        reviewDTO.setRating(4.0);
        reviewDTO.setComment("Great place!");

        when(hotelRepository.findById("1")).thenReturn(Optional.of(testHotel));

        hotelService.addReview("1", reviewDTO);

        assertEquals(1, testHotel.getGuestReviews().size());
        assertEquals("1", testHotel.getGuestReviews().getFirst().getUser());
        assertEquals(4.0, testHotel.getGuestReviews().getFirst().getGuestRating());
        assertEquals("Great place!", testHotel.getGuestReviews().getFirst().getComment());
    }

    @Test
     void testAddRoom() {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setRoomType("Suite");
        roomDTO.setRoomSpecification("Luxury suite with city view");
        roomDTO.setRoomCount(5);
        roomDTO.setRoomRate(300);

        when(hotelRepository.findById("1")).thenReturn(Optional.of(testHotel));

        hotelService.addRoom("1", roomDTO);

        assertEquals(1, testHotel.getRooms().size());
        assertEquals("Suite", testHotel.getRooms().getFirst().getRoomType());
        assertEquals("Luxury suite with city view", testHotel.getRooms().get(0).getRoomSpecification());
        assertEquals(5, testHotel.getRooms().getFirst().getRoomCount());
        assertEquals(300.0, testHotel.getRooms().getFirst().getRoomRate());
    }



    @Test
     void testFindHotels_noHotelsFound() {
        when(hotelRepository.findByLocationCityName(anyString())).thenReturn(new ArrayList<>());

        SearchResultDTO searchResultDTO = hotelService.findHotels("New York", LocalDate.now(), LocalDate.now().plusDays(3), 2, 400, 200, 4.0, null);

        assertEquals(0, searchResultDTO.getHotels().size());
    }

    @Test
     void testFindHotels_checkInCheckOutDates() {
        testHotel.setId("1");
        testHotel.setHotelName("Test Hotel");
        testHotel.setRooms(new ArrayList<>());
        testHotel.getRooms().add(new Room("1", "Suite", "Luxury suite with city view", 5, 300, null));

        List<Hotel> dummyHotels = new ArrayList<>();
        dummyHotels.add(testHotel);

        when(hotelRepository.findByLocationCityName("New York")).thenReturn(dummyHotels);

        SearchResultDTO searchResultDTO = hotelService.findHotels("New York", LocalDate.now(), LocalDate.now().plusDays(3), 2, 400, 200, 4.0, null);

        assertEquals(1, searchResultDTO.getHotels().size());
        assertEquals(1, searchResultDTO.getRoomIds().size()); // One room should be available
        assertEquals("1", searchResultDTO.getRoomIds().getFirst());
    }

    // Additional test cases can be added to cover more scenarios

}