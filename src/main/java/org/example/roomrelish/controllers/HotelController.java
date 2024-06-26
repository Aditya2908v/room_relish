package org.example.roomrelish.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.roomrelish.dto.*;
import org.example.roomrelish.models.Hotel;
import org.example.roomrelish.services.hotel.HotelService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/hotel")
@Tag(name = "Hotel Management")
public class HotelController {

    private final HotelService hotelService;

    // GraphQL endpoints
    @QueryMapping("hotels")
    public List<Hotel> getAllHotelsGraphQL(){
        return hotelService.getAllHotels();
    }

    @QueryMapping("hotel")
    public Hotel getHotelGraphQL(@Argument String id){
        return hotelService.findHotelById(id);
    }

    @GetMapping("/hotels")
    public List<Hotel> getAllHotels(){
        return hotelService.getAllHotels();
    }

    @GetMapping("/hotel")
    public Hotel getHotel(@RequestParam String id){
        return hotelService.findHotelById(id);
    }

    // Search hotel
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
    public ResponseEntity<SearchResultDTO> searchHotels(
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false, defaultValue = "1") int countOfRooms,
            @RequestParam(required = false, defaultValue = "0") int priceRangeMax,
            @RequestParam(required = false, defaultValue = "0") int priceRangeMin,
            @RequestParam(required = false, defaultValue = "0") double rating,
            @RequestParam(required = false) List<String> amenities
    ){
        SearchResultDTO searchResultDTO = hotelService.findHotels(cityName, checkInDate, checkOutDate, countOfRooms, priceRangeMax, priceRangeMin, rating, amenities);
        return ResponseEntity.ok(searchResultDTO);
    }

    @PostMapping("/createHotel")
    public ResponseEntity<String> createHotel(@Valid @RequestBody HotelDTO hotelDTO){
        hotelService.createHotel(hotelDTO);
        return ResponseEntity.ok("Hotel Created Successfully");
    }

    @PutMapping("/updateHotel")
    public ResponseEntity<String> updateHotel(@RequestParam String id, @Valid @RequestBody HotelDTO hotelDTO){
        hotelService.updateHotel(id, hotelDTO);
        return ResponseEntity.ok("Hotel Updated Successfully");
    }

    @DeleteMapping("/deleteHotel")
    public ResponseEntity<String> deleteHotel(@RequestParam String id){
        hotelService.deleteHotel(id);
        return ResponseEntity.ok("Hotel deleted successfully");
    }

    // Get reviews
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponse>> getAllReviews(@RequestParam String id){
        List<ReviewResponse> guestReviews = hotelService.getReviews(id);
        return ResponseEntity.ok(guestReviews);
    }

    // Add review
    @PostMapping("/addReview")
    public ResponseEntity<String> addReview(@RequestParam String id, @Valid @RequestBody ReviewDTO reviewDTO) {
        hotelService.addReview(id, reviewDTO);
        return ResponseEntity.ok("Review added successfully");
    }

    // Add room
    @PostMapping("/addRoom")
    public ResponseEntity<String> addRoom(@RequestParam String id, @Valid @RequestBody RoomDTO roomDTO){
        hotelService.addRoom(id, roomDTO);
        return ResponseEntity.ok("Room added successfully");
    }
}
