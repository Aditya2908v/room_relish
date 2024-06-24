package org.example.roomrelish.services.customer;

import org.example.roomrelish.dto.CardDTO;
import org.example.roomrelish.dto.CustomerProfile;
import org.example.roomrelish.dto.UpdateCustomerDTO;
import org.example.roomrelish.exception.CustomerAlreadyExistsException;
import org.example.roomrelish.exception.ResourceNotFoundException;
import org.example.roomrelish.models.Customer;
import org.example.roomrelish.models.Hotel;
import org.example.roomrelish.repository.CustomerRepository;
import org.example.roomrelish.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest {
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private CustomerServiceImpl customerService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddCardToCustomer(){
        String email = "test@example.com";
        CardDTO cardDTO = new CardDTO();
        cardDTO.setCardNumber("123");
        cardDTO.setCardHolder("John Doe");
        cardDTO.setExpirationDate("12/25");
        cardDTO.setCardName("Visa");

        Customer customer = new Customer();
        customer.setEmail(email);
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        customerService.addCardToUser(cardDTO, email);

        verify(customerRepository, times(1)).save(customer);
    }

    @Test
     void testAddCardToCustomer_CustomerNotFound() {
        // Mock dependencies or set up necessary objects for the test

        CardDTO cardDTO = new CardDTO();
        cardDTO.setCardNumber("1234567890123456");
        cardDTO.setCardHolder("John Doe");
        cardDTO.setExpirationDate("12/24");
        cardDTO.setCvv("123");
        cardDTO.setCardName("Visa");

        String userEmail = "nonexistent@example.com";

        // Assert that calling addCardToUser with a non-existent email throws ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.addCardToUser(cardDTO, userEmail);
        });

    }

    @Test
    void testGetAllCustomers(){
        List<Customer> customers = List.of(new Customer(), new Customer());
        when(customerRepository.findAll()).thenReturn(customers);
        List<Customer> customerList = customerService.getAllCustomers();
        assertEquals(customers.size(), customerList.size());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void testUpdateCustomer_Success() {
        // Mock data
        String userEmail = "user@example.com";
        UpdateCustomerDTO updateCustomerDTO = new UpdateCustomerDTO();
        updateCustomerDTO.setPhoneNumber("1234567890");
        updateCustomerDTO.setPassword("newPassword");

        Customer existingCustomer = new Customer();
        existingCustomer.setEmail(userEmail);

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByPhoneNumber(updateCustomerDTO.getPhoneNumber())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(updateCustomerDTO.getPassword())).thenReturn("encodedPassword");

        // Call the service method
        customerService.updateCustomer(userEmail, updateCustomerDTO);

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
        verify(customerRepository, times(1)).findByPhoneNumber(updateCustomerDTO.getPhoneNumber());
        verify(customerRepository, times(1)).save(existingCustomer);
        verify(passwordEncoder, times(1)).encode(updateCustomerDTO.getPassword());

    }


    @Test
    void testUpdateCustomer_PhoneNumberAlreadyExists() {
        // Mock data
        String userEmail = "user@example.com";
        UpdateCustomerDTO updateCustomerDTO = new UpdateCustomerDTO();
        updateCustomerDTO.setPhoneNumber("9441898506");

        Customer existingCustomer = new Customer();
        existingCustomer.setId("1"); // Ensure id is initialized
        existingCustomer.setEmail(userEmail);

        Customer existingCustomerWithPhoneNumber = new Customer();
        existingCustomerWithPhoneNumber.setId("2"); // Ensure id is initialized
        existingCustomerWithPhoneNumber.setEmail("existing@example.com");
        existingCustomerWithPhoneNumber.setPhoneNumber(updateCustomerDTO.getPhoneNumber());

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByPhoneNumber(updateCustomerDTO.getPhoneNumber())).thenReturn(Optional.of(existingCustomerWithPhoneNumber));

        // Expect CustomerAlreadyExistsException
        assertThrows(CustomerAlreadyExistsException.class, () -> {
            customerService.updateCustomer(userEmail, updateCustomerDTO);
        });

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
        verify(customerRepository, times(1)).findByPhoneNumber(updateCustomerDTO.getPhoneNumber());
        verify(customerRepository, never()).save(existingCustomer);
        verify(passwordEncoder, never()).encode(any());
    }


    @Test
    void testUpdateCustomer_CustomerNotFound() {
        // Mock data
        String userEmail = "user@example.com";
        UpdateCustomerDTO updateCustomerDTO = new UpdateCustomerDTO();
        updateCustomerDTO.setPhoneNumber("9441898506");

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Expect ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.updateCustomer(userEmail, updateCustomerDTO);
        });

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
        verify(customerRepository, never()).findByPhoneNumber(any());
        verify(customerRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }



    @Test
    void testGetProfileInfo_FullDetails() {
        // Mock data
        String userEmail = "user@example.com";

        Customer customer = new Customer();
        customer.setId("1");
        customer.setUsername("username");
        customer.setEmail("user@example.com");
        customer.setPhoneNumber("1234567890");
        customer.setPassword("password");
        customer.setDateOfBirth(new Date());
        customer.setAddress("123 Street, City, Country");

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.of(customer));

        // Call the service method
        CustomerProfile result = customerService.getProfileInfo(userEmail);

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);

    }

    @Test
    void testGetProfileInfo_CustomerNotFound() {
        // Mock data
        String userEmail = "nonexistent@example.com";

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Define expected exception
        assertThrows(ResourceNotFoundException.class, () -> {
            // Call the service method
            customerService.getFavouriteHotels(userEmail);
        });


        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
    }

    @Test
    void testGetProfilePicture_CustomerFound() {
        // Mock data
        String userEmail = "user@example.com";
        String profilePicture = "profile.jpg";

        Customer customer = new Customer();
        customer.setProfilePicture(profilePicture);

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.of(customer));

        // Call the service method
        String result = customerService.getProfilePicture(userEmail);

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);

        // Verify result
        assertEquals(profilePicture, result);
    }

    @Test
    void testGetProfilePicture_CustomerNotFound() {
        // Mock data
        String userEmail = "nonexistent@example.com";

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Call the service method
        String result = customerService.getProfilePicture(userEmail);

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);

        // Verify result
        assertNull(result);
    }




    @Test
    void testGetFavouriteHotels_Success() {
        // Mock data
        String userEmail = "user@example.com";

        Customer customer = new Customer();
        customer.setFavouriteHotels(List.of("hotel1", "hotel2"));

        Hotel hotel1 = new Hotel();
        hotel1.setId("hotel1");

        Hotel hotel2 = new Hotel();
        hotel2.setId("hotel2");

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.of(customer));
        when(hotelRepository.findById("hotel1")).thenReturn(Optional.of(hotel1));
        when(hotelRepository.findById("hotel2")).thenReturn(Optional.of(hotel2));

        // Call the service method
        List<Hotel> result = customerService.getFavouriteHotels(userEmail);

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
        verify(hotelRepository, times(1)).findById("hotel1");
        verify(hotelRepository, times(1)).findById("hotel2");

        // Verify result
        assertEquals(2, result.size());
        assertEquals("hotel1", result.get(0).getId());
        assertEquals("hotel2", result.get(1).getId());
    }


    @Test
    void testGetFavouriteHotels_CustomerNotFound() {
        // Mock data
        String userEmail = "nonexistent@example.com";

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Define expected exception
        assertThrows(ResourceNotFoundException.class, () -> {
            // Call the service method
            customerService.getFavouriteHotels(userEmail);
        });

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
        verify(hotelRepository, never()).findById(any());
    }



    @Test
    void testGetFavouriteHotels_HotelNotFound() {
        // Mock data
        String userEmail = "user@example.com";

        Customer customer = new Customer();
        customer.setFavouriteHotels(List.of("hotel1", "hotel2"));

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.of(customer));
        when(hotelRepository.findById("hotel1")).thenReturn(Optional.empty());
        when(hotelRepository.findById("hotel2")).thenReturn(Optional.empty());

        // Call the service method
        List<Hotel> result = customerService.getFavouriteHotels(userEmail);

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
        verify(hotelRepository, times(1)).findById("hotel1");
        verify(hotelRepository, times(1)).findById("hotel2");

        // Verify result
        assertEquals(0, result.size());
    }

    @Test
    void testAddFavouriteHotel_Success() {
        // Mock data
        String userEmail = "user@example.com";
        String hotelId = "hotel1";

        Customer customer = new Customer();
        List<String> favouriteHotelIds = new ArrayList<>();
        customer.setFavouriteHotels(favouriteHotelIds);

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.of(customer));

        // Call the service method
        customerService.addFavouriteHotel(userEmail, hotelId);

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
        verify(customerRepository, times(1)).save(customer);

        // Verify result
        assertTrue(customer.getFavouriteHotels().contains(hotelId));
    }

    @Test
    void testAddFavouriteHotel_CustomerNotFound() {
        // Mock data
        String userEmail = "nonexistent@example.com";

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Define expected exception
        assertThrows(ResourceNotFoundException.class, () -> {
            // Call the service method
            customerService.getFavouriteHotels(userEmail);
        });

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void testAddFavouriteHotel_HotelAlreadyFavourited() {
        // Mock data
        String userEmail = "user@example.com";
        String hotelId = "hotel1";

        Customer customer = new Customer();
        List<String> favouriteHotelIds = new ArrayList<>();
        favouriteHotelIds.add(hotelId);
        customer.setFavouriteHotels(favouriteHotelIds);

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.of(customer));

        // Call the service method
        customerService.addFavouriteHotel(userEmail, hotelId);

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void testDeleteFavouriteHotel_Success() {
        // Mock data
        String userEmail = "user@example.com";
        String hotelId = "hotel1";

        Customer customer = new Customer();
        List<String> favouriteHotelIds = new ArrayList<>();
        favouriteHotelIds.add(hotelId);
        customer.setFavouriteHotels(favouriteHotelIds);

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.of(customer));

        // Call the service method
        customerService.deleteFavouriteHotel(userEmail, hotelId);

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
        verify(customerRepository, times(1)).save(customer);

        // Verify result
        assertFalse(customer.getFavouriteHotels().contains(hotelId));
    }

    @Test
    void testDeleteFavouriteHotel_CustomerNotFound() {
        // Mock data
        String userEmail = "nonexistent@example.com";
        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.empty());
        // Define expected exception
        assertThrows(ResourceNotFoundException.class, () -> {
            // Call the service method
            customerService.getFavouriteHotels(userEmail);
        });

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void testDeleteFavouriteHotel_HotelNotFavorited() {
        // Mock data
        String userEmail = "user@example.com";
        String hotelId = "hotel1";

        Customer customer = new Customer();

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.of(customer));

        // Call the service method
        customerService.deleteFavouriteHotel(userEmail, hotelId);

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void testFindRecentHotels_Success() {
        // Mock data
        String userEmail = "user@example.com";

        Customer customer = new Customer();
        List<String> recentHotelIds = new ArrayList<>();
        recentHotelIds.add("hotel1");
        recentHotelIds.add("hotel2");
        recentHotelIds.add("hotel3");
        customer.setRecentVisitsOfHotels(recentHotelIds);

        Hotel hotel1 = new Hotel();
        hotel1.setId("hotel1");

        Hotel hotel2 = new Hotel();
        hotel2.setId("hotel2");

        Hotel hotel3 = new Hotel();
        hotel3.setId("hotel3");

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.of(customer));
        when(hotelRepository.findById("hotel1")).thenReturn(Optional.of(hotel1));
        when(hotelRepository.findById("hotel2")).thenReturn(Optional.of(hotel2));
        when(hotelRepository.findById("hotel3")).thenReturn(Optional.of(hotel3));

        // Call the service method
        List<Hotel> result = customerService.findRecentHotels(userEmail);

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
        verify(hotelRepository, times(1)).findById("hotel1");
        verify(hotelRepository, times(1)).findById("hotel2");
        verify(hotelRepository, times(1)).findById("hotel3");

        // Verify result
        assertEquals(3, result.size());
    }

    @Test
    void testFindRecentHotels_CustomerNotFound() {
        // Mock data
        String userEmail = "nonexistent@example.com";

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Define expected exception
        assertThrows(ResourceNotFoundException.class, () -> {
            // Call the service method
            customerService.getFavouriteHotels(userEmail);
        });


        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
        verify(hotelRepository, never()).findById(any());
    }

    @Test
    void testFindRecentHotels_HotelNotFound() {
        // Mock data
        String userEmail = "user@example.com";

        Customer customer = new Customer();
        List<String> recentHotelIds = new ArrayList<>();
        recentHotelIds.add("hotel1");
        customer.setRecentVisitsOfHotels(recentHotelIds);

        // Mock repository behavior
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.of(customer));
        when(hotelRepository.findById("hotel1")).thenReturn(Optional.empty());

        // Call the service method
        List<Hotel> result = customerService.findRecentHotels(userEmail);

        // Verify repository method is called with the correct parameter
        verify(customerRepository, times(1)).findByEmail(userEmail);
        verify(hotelRepository, times(1)).findById("hotel1");

        // Verify result
        assertEquals(0, result.size());
    }

}
