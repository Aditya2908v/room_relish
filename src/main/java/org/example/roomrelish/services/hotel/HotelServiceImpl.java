package org.example.roomrelish.services.hotel;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.example.roomrelish.dto.*;
import org.example.roomrelish.exception.ResourceNotFoundException;
import org.example.roomrelish.models.*;
import org.example.roomrelish.repository.CustomerRepository;
import org.example.roomrelish.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final CustomerRepository customerRepository;
    String hotelErrorMessage = "Hotel not found";

    @Override
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    @Override
    public Hotel findHotelById(String id) {
        return hotelRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel", "Hotel Id", id));
    }

    @Override
    public void createHotel(HotelDTO hotelDTO) {
        if (hotelDTO == null) {
            throw new IllegalArgumentException("Invalid hotel details");
        }
        Hotel hotel = HotelMapper.INSTANCE.toHotel(hotelDTO);
        hotelRepository.save(hotel);
    }


    @Override
    public void updateHotel(String id, HotelDTO hotelDTO) {
        if (hotelDTO == null) {
            throw new IllegalArgumentException("Invalid Hotel Details");
        }

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(hotelErrorMessage));

        HotelMapper.INSTANCE.updateHotelFromDTO(hotelDTO, hotel);

    }

    @Override
    public void deleteHotel(String id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel", "Hotel Id", id));
        hotelRepository.delete(hotel);
    }

    @Override
    public List<ReviewResponse> getReviews(String id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "Hotel Id", id));

         List<GuestReview> guestReviews = hotel.getGuestReviews();
        List<ReviewResponse> reviewResponses = new ArrayList<>();

        for (GuestReview guestReview : guestReviews) {
            String customerId = guestReview.getUser();
            Customer customer = customerRepository.findById(customerId).orElse(null);

            if (customer != null) {
                ReviewResponse reviewResponse = ReviewResponse.builder()
                        .customerName(customer.getUserName())
                        .guestRating(guestReview.getGuestRating())
                        .comment(guestReview.getComment())
                        .build();
                reviewResponses.add(reviewResponse);
            }
        }
        return reviewResponses;
    }


    @Override
    public void addReview(String id, ReviewDTO reviewDTO) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "Hotel Id", id));

        if (hotel.getGuestReviews() == null)
            hotel.setGuestReviews(new ArrayList<>());

        GuestReview guestReview = GuestReview.builder()
                .user(reviewDTO.getUserid())
                .guestRating(reviewDTO.getRating())
                .comment(reviewDTO.getComment())
                .build();
        hotel.getGuestReviews().add(guestReview);
        hotelRepository.save(hotel);
    }

    @Transactional
    @Override
    public void addRoom(String id, RoomDTO roomDTO) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "Hotel id", id));
        Room room = Room.builder()
                .id(new ObjectId().toString())
                .roomType(roomDTO.getRoomType())
                .roomSpecification(roomDTO.getRoomSpecification())
                .roomCount(roomDTO.getRoomCount())
                .roomRate(roomDTO.getRoomRate())
                .build();
        List<Room> rooms = hotel.getRooms();
        if (rooms == null) {
            rooms = new ArrayList<>();
            hotel.setRooms(rooms);
        }
        rooms.add(room);
        hotelRepository.save(hotel);
    }


    public SearchResultDTO findHotels(String cityName,
                                      LocalDate checkInDate,
                                      LocalDate checkOutDate,
                                      int countOfRooms,
                                      int priceRangeMax,
                                      int priceRangeMin,
                                      double rating,
                                      List<String> amenities) {
        try {
            List<Hotel> filteredHotels = hotelRepository.findByLocationCityName(cityName);
            filteredHotels = filteringHotelsByAmenities(filteredHotels, amenities);

            filteredHotels = filteringHotelsByRating(filteredHotels, rating);

            return filteringHotelsByCheckInCheckOutDate(filteredHotels, checkInDate, checkOutDate, countOfRooms);

        } catch (Exception e) {
            throw new IllegalArgumentException("An error occurred while searching for hotels.", e);
        }
    }

    public SearchResultDTO filteringHotelsByCheckInCheckOutDate(List<Hotel> filteredHotels, LocalDate checkInDate, LocalDate checkOutDate, int countOfRooms) {

        List<String> availableRoomIds = new ArrayList<>();
        if ((checkInDate != null) && (checkOutDate != null)) {
            findAvailability(checkInDate, checkOutDate, countOfRooms, availableRoomIds, filteredHotels);
        }
        LinkedHashSet<String> set = new LinkedHashSet<>(availableRoomIds);
        ArrayList<String> availableRoomIdsList = new ArrayList<>(set);
        SearchResultDTO searchResultDTO = new SearchResultDTO();
        searchResultDTO.setHotels(filteredHotels);
        searchResultDTO.setRoomIds(availableRoomIdsList);

        return searchResultDTO;
    }

    public void findAvailability(LocalDate userCheckInDate, LocalDate userCheckOutDate, int countOfRooms, List<String> availableRoomIds, List<Hotel> filteredHotels) {
        availableRoomIds.addAll(
                filteredHotels.stream().flatMap(hotel -> hotel.getRooms().stream()).filter(room -> {
                    int initialRoomCount = room.getRoomCount();
                    int roomCount;
                    if (room.getRoomAvailabilityList() != null) {
                        roomCount = room.getRoomAvailabilityList().stream()
                                .reduce(initialRoomCount,
                                        (result, availability) -> findAvailabilityWithTheList(availability, userCheckInDate, userCheckOutDate, result),
                                        Integer::sum);
                    } else {
                        roomCount = initialRoomCount;
                    }
                    return room.getRoomAvailabilityList() == null || roomCount > countOfRooms;
                }).map(Room::getId).toList()
        );
    }

    public int findAvailabilityWithTheList(RoomAvailability availability, LocalDate userCheckInDate, LocalDate userCheckOutDate, int roomCount) {
        if (((userCheckInDate.isBefore(availability.getCheckOutDate())) || (userCheckInDate.isEqual(availability.getCheckOutDate())))
                && ((userCheckOutDate.isAfter(availability.getCheckInDate())) || (userCheckInDate.isEqual(availability.getCheckInDate())))) {
            roomCount = roomCount - availability.getRoomCount();
        }
        return roomCount;
    }

    private List<Hotel> filteringHotelsByRating(List<Hotel> filteredHotels, double rating) {
        if (rating > 0) {
            filteredHotels = filteredHotels.stream().filter(hotel -> hotel.getRating() > rating).collect(Collectors.toList());
        }
        return filteredHotels;
    }

    private List<Hotel> filteringHotelsByAmenities(List<Hotel> filteredHotels, List<String> amenities) {
        if (amenities != null) {
            filteredHotels = filteredHotels.stream().filter(hotel -> new HashSet<>(hotel.getAmenities()).containsAll(amenities)).toList();
        }
        return filteredHotels;
    }

}


