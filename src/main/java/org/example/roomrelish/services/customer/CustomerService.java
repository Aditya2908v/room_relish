package org.example.roomrelish.services.customer;

import org.example.roomrelish.dto.CardDTO;
import org.example.roomrelish.dto.CustomerProfile;
import org.example.roomrelish.dto.UpdateCustomerDTO;
import org.example.roomrelish.models.Customer;
import org.example.roomrelish.models.Hotel;
import org.jetbrains.annotations.TestOnly;

import java.util.List;

@TestOnly
public interface CustomerService {
    void addCardToUser(CardDTO cardDTO,String userEmail);

    List<Customer> getAllCustomers();

    void updateCustomer(String userEmail, UpdateCustomerDTO updateCustomerDTO);

    CustomerProfile getProfileInfo(String userEmail);

    String getProfilePicture(String userEmail);

    List<Hotel> getFavouriteHotels(String userEmail);

    void addFavouriteHotel(String userEmail, String hotelId);

    void deleteFavouriteHotel(String userEmail, String hotelId);

    List<Hotel> findRecentHotels(String userEmail);
}
