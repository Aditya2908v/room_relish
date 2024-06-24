package org.example.roomrelish.services.customer;

import lombok.RequiredArgsConstructor;
import org.example.roomrelish.exception.CustomerAlreadyExistsException;
import org.example.roomrelish.exception.ResourceNotFoundException;
import org.example.roomrelish.dto.CardDTO;
import org.example.roomrelish.dto.CustomerProfile;
import org.example.roomrelish.dto.UpdateCustomerDTO;
import org.example.roomrelish.models.Card;
import org.example.roomrelish.models.Customer;
import org.example.roomrelish.models.Hotel;
import org.example.roomrelish.repository.CustomerRepository;
import org.example.roomrelish.repository.HotelRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

//@SuppressWarnings("all")
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final HotelRepository hotelRepository;
    String errorMessageCustomer = "Customer not found";

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public void addCardToUser(CardDTO cardDTO, String userEmail) {
        Optional<Customer> optionalCustomer = customerRepository.findByEmail(userEmail);
        Customer customer = optionalCustomer.orElseThrow(() -> new ResourceNotFoundException("Customer", "email", userEmail));
        Card card = new Card();
        card.setCardNumber(cardDTO.getCardNumber());
        card.setCardHolder(cardDTO.getCardHolder());
        card.setExpirationDate(cardDTO.getExpirationDate());
        card.setCvv(cardDTO.getCvv());
        card.setCardName(cardDTO.getCardName());
        List<Card> cards = customer.getAddedCards();
        if (cards == null) {
            cards = new ArrayList<>();
        }
        cards.add(card);
        customer.setAddedCards(cards);
        customerRepository.save(customer);
    }

    @Override
    public void updateCustomer(String userEmail, UpdateCustomerDTO updateCustomerDTO) {
        Customer customer = customerRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", userEmail));
            if (updateCustomerDTO.getPhoneNumber() != null && !updateCustomerDTO.getPhoneNumber().equals(customer.getPhoneNumber())) {
                Optional<Customer> existingUserWithPhoneNumber = customerRepository.findByPhoneNumber(updateCustomerDTO.getPhoneNumber());
                if (existingUserWithPhoneNumber.isPresent() && !existingUserWithPhoneNumber.get().getId().equals(customer.getId())) {
                    throw new CustomerAlreadyExistsException("Customer","phone number",updateCustomerDTO.getPhoneNumber());
                }
                customer.setPhoneNumber(updateCustomerDTO.getPhoneNumber());
            }
            if (updateCustomerDTO.getPassword() != null && !updateCustomerDTO.getPassword().equals(customer.getPassword())) {
                customer.setPassword(passwordEncoder.encode(updateCustomerDTO.getPassword()));
            }
            customerRepository.save(customer);
    }

    @Override
    public CustomerProfile getProfileInfo(String userEmail) {
        Customer customer = customerRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", userEmail));
        return CustomerProfile.builder()
                .id(customer.getId())
                .username(customer.getUserName())
                .profilePicture(customer.getProfilePicture())
                .build();
    }

    @Override
    public String getProfilePicture(String userEmail) {
        Optional<Customer> customer = customerRepository.findByEmail(userEmail);
        return customer.map(Customer::getProfilePicture).orElse(null);
    }


    @Override
    public List<Hotel> getFavouriteHotels(String userEmail) {
        Customer customer = customerRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", userEmail));
        List<String> hotelIds = customer.getFavouriteHotels();
        if(hotelIds == null) {
            return Collections.emptyList();
        }
        return hotelIds.stream()
                .map(hotelRepository::findById)
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public void addFavouriteHotel(String userEmail, String hotelId) {
        Customer customer = customerRepository.findByEmail(userEmail)
                .orElseThrow(()-> new ResourceNotFoundException("Customer", "Customer email", userEmail));
        List<String> favouriteHotelIds = customer.getFavouriteHotels() != null
                ? customer.getFavouriteHotels() : new ArrayList<>();
        if (!favouriteHotelIds.contains(hotelId)) {
            favouriteHotelIds.add(hotelId);
            customer.setFavouriteHotels(favouriteHotelIds);
            customerRepository.save(customer);
        }
    }

    @Override
    public void deleteFavouriteHotel(String userEmail, String hotelId) {
        Customer customer = customerRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "Customer email", userEmail));

        List<String> favouriteHotelIds = customer.getFavouriteHotels();

        if (favouriteHotelIds != null && favouriteHotelIds.contains(hotelId)) {
            favouriteHotelIds.remove(hotelId);
            customer.setFavouriteHotels(favouriteHotelIds);
            customerRepository.save(customer);
        }
    }


    @Override
    public List<Hotel> findRecentHotels(String userEmail) {
        Customer customer = customerRepository.findByEmail(userEmail)
                .orElseThrow(()-> new ResourceNotFoundException("Customer", "Customer email", userEmail));
        List<String> recentHotelIds = customer.getRecentVisitsOfHotels();
        Collections.reverse(recentHotelIds);
        return recentHotelIds.stream()
                .map(hotelRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
