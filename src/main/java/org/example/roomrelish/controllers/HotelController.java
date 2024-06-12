package org.example.roomrelish.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.roomrelish.dto.*;
import org.example.roomrelish.models.GuestReview;
import org.example.roomrelish.models.Hotel;
import org.example.roomrelish.models.Room;
import org.example.roomrelish.services.HotelService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/hotels")
@Tag(name = "Hotel Management")
public class HotelController {

    private final HotelService hotelService;

    @GetMapping("/hello")
    public String hello(){
        return "Hello World";
    }

    //GraphQL endpoints
    @QueryMapping("hotels")
    public List<Hotel> getAllHotelsGraphQL(){
        return hotelService.getAllHotels();
    }

    @QueryMapping("hotel")
    public Hotel getHotelGraphQL(@Argument String id){
        return hotelService.findHotelById(id);
    }

    //search hotel
    @Operation(
            description = "Search Hotels",
            summary = "Search hotels by city name and/or rating",
            responses = {
                    @ApiResponse(
                            description = "List of hotels found",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "No hotels found",
                            responseCode = "204"
                    ),
                    @ApiResponse(
                            description = "Internal server error",
                            responseCode = "500"
                    )
            }
    )
    @GetMapping("/search")
    public ResponseEntity<?> searchHotels(
            @RequestParam String cityName,
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate,
            @RequestParam int countOfRooms,
            @RequestParam int priceRangeMax,
            @RequestParam int priceRangeMin,
            @RequestParam double rating,
            @RequestParam List<String> amenities
    ){
        SearchResultDTO searchResultDTO = hotelService.findHotels(cityName,checkInDate,checkOutDate,countOfRooms,priceRangeMax,priceRangeMin,rating,amenities);
        return ResponseEntity.ok(searchResultDTO);
    }

    @PostMapping
    public ResponseEntity<?> createHotel(@Valid @RequestBody HotelDTO hotelDTO){
        Hotel hotel = hotelService.createHotel(hotelDTO);
        return ResponseEntity.ok(hotel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHotel(@PathVariable String id, @Valid @RequestBody HotelDTO hotelDTO){
        Hotel hotel = hotelService.updateHotel(id,hotelDTO);
        return ResponseEntity.ok(hotel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHotel(@PathVariable String id){
        hotelService.deleteHotel(id);
        return ResponseEntity.ok("Hotel deleted successfully");
    }

    //Get reviews
    @GetMapping("{id}/reviews")
    public ResponseEntity<?> getAllReviews(@PathVariable String id){
        List<ReviewResponse> guestReviews = hotelService.getReviews(id);
        return ResponseEntity.ok(guestReviews);
    }

    //add review
    @PostMapping("{id}/reviews")
    public ResponseEntity<?> addReview(@PathVariable String id, @Valid @RequestBody ReviewDTO reviewDTO) {
        GuestReview guestReview = hotelService.addReview(id, reviewDTO);
        return ResponseEntity.ok(guestReview);
    }

    //delete review
    @DeleteMapping("{hotelId}/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable String hotelId, @PathVariable String reviewId) {
        hotelService.deleteReview(hotelId, reviewId);
        return ResponseEntity.ok("Review deleted successfully");
    }

    //add room
    @PostMapping("{id}/rooms")
    public ResponseEntity<?> addRoom(@PathVariable String id, @Valid @RequestBody RoomDTO roomDTO){
        Room room = hotelService.addRoom(id, roomDTO);
        return ResponseEntity.ok(room);
    }

}
