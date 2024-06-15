package org.example.roomrelish.repository;

import org.example.roomrelish.models.Hotel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;


public interface HotelRepository extends MongoRepository<Hotel, String> {
    Optional<Hotel> findById(String hotelId);
    List<Hotel> findByLocationCityNameAndRatingGreaterThanEqual(
            String location_cityName, double rating);
    List<Hotel> findByLocationCityName(String cityName);
}
