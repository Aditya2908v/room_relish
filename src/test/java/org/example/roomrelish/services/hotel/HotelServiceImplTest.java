package org.example.roomrelish.services.hotel;

import org.example.roomrelish.dto.HotelDTO;
import org.example.roomrelish.dto.ReviewDTO;
import org.example.roomrelish.dto.ReviewResponse;
import org.example.roomrelish.exception.ResourceNotFoundException;
import org.example.roomrelish.models.Customer;
import org.example.roomrelish.models.GuestReview;
import org.example.roomrelish.models.Hotel;
import org.example.roomrelish.repository.CustomerRepository;
import org.example.roomrelish.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId("1");
        hotel.setHotelName("Test Hotel");
    }

    @Test
    void testGetAllHotels() {
        List<Hotel> hotels = List.of(hotel);
        when(hotelRepository.findAll()).thenReturn(hotels);

        List<Hotel> result = hotelService.getAllHotels();

        assertEquals(hotels.size(), result.size());
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void testFindHotelById() {
        when(hotelRepository.findById(anyString())).thenReturn(Optional.of(hotel));

        Hotel result = hotelService.findHotelById("1");

        assertNotNull(result);
        assertEquals(hotel.getHotelName(), result.getHotelName());
        verify(hotelRepository, times(1)).findById("1");
    }

    @Test
    void testFindHotelByIdNotFound() {
        when(hotelRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> hotelService.findHotelById("1"));
        verify(hotelRepository, times(1)).findById("1");
    }

    @Test
    void testCreateHotel() {
        HotelDTO hotelDTO = new HotelDTO();
        hotelDTO.setHotelName("New Hotel");

        hotelService.createHotel(hotelDTO);

        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void testUpdateHotel() {
        HotelDTO hotelDTO = new HotelDTO();
        hotelDTO.setHotelName("Updated Hotel");

        when(hotelRepository.findById(anyString())).thenReturn(Optional.of(hotel));

        hotelService.updateHotel("1", hotelDTO);

        verify(hotelRepository, times(1)).findById("1");
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void testDeleteHotel() {
        when(hotelRepository.findById(anyString())).thenReturn(Optional.of(hotel));

        hotelService.deleteHotel("1");

        verify(hotelRepository, times(1)).findById("1");
        verify(hotelRepository, times(1)).delete(hotel);
    }

    @Test
    void testGetReviews() {
        GuestReview review = new GuestReview();
        review.setUser("1");
        review.setGuestRating(4.5);
        review.setComment("Great stay!");
        hotel.setGuestReviews(List.of(review));

        Customer customer = new Customer();
        customer.setId("1");

        when(hotelRepository.findById(anyString())).thenReturn(Optional.of(hotel));
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(customer));

        List<ReviewResponse> result = hotelService.getReviews("1");

        assertEquals(1, result.size());
        verify(hotelRepository, times(1)).findById("1");
        verify(customerRepository, times(1)).findById("1");
    }

    @Test
    void testAddReview() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setUserid("1");
        reviewDTO.setRating(4.5);
        reviewDTO.setComment("Great stay!");

        when(hotelRepository.findById(anyString())).thenReturn(Optional.of(hotel));

        hotelService.addReview("1", reviewDTO);

        assertEquals(1, hotel.getGuestReviews().size());
        verify(hotelRepository, times(1)).findById("1");
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }
}
