package org.example.roomrelish.services.hotel;

import org.example.roomrelish.dto.*;
import org.example.roomrelish.models.GuestReview;
import org.example.roomrelish.models.Hotel;
import org.example.roomrelish.models.Room;
import org.jetbrains.annotations.TestOnly;

import java.time.LocalDate;
import java.util.List;

@TestOnly
public interface HotelService {
    List<Hotel> getAllHotels();
    Hotel findHotelById(String id);
    void createHotel(HotelDTO hotelDTO);
    void updateHotel(String id,HotelDTO hotelDTO);
    List<ReviewResponse> getReviews(String id);
    void addReview(String id, ReviewDTO reviewDTO);
    void deleteHotel(String id);
    void addRoom(String id, RoomDTO roomDTO);

    SearchResultDTO findHotels(String cityName,
                           LocalDate checkInDate,
                           LocalDate checkOutDate,
                           int countOfRooms,
                           int priceRangeMax,
                           int priceRangeMin,
                           double rating,
                           List<String> amenities
    );

}
