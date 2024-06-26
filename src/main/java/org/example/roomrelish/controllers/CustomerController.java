package org.example.roomrelish.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.roomrelish.dto.*;
import org.example.roomrelish.models.Customer;
import org.example.roomrelish.models.Hotel;
import org.example.roomrelish.services.authentication.AuthService;
import org.example.roomrelish.services.authentication.JwtService;
import org.example.roomrelish.services.customer.CustomerService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final AuthService authService;
    private final JwtService jwtService;

    @GetMapping("/hello")
    public String getMessage(){
        return "hello world";
    }

    @QueryMapping("users")
    public List<Customer> getAllCustomersGraphQL() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @Operation(
            description = "User Registration",
            summary = "Registers a new Customer",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User Registration Successful"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request. The request body is invalid or missing required fields"
                    )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerCustomer(@RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.registerCustomer(registerRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(
            description = "User Authentication",
            summary = "Authenticates a Customer",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User Authentication Successful"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "User not found or incorrect credentials."
                    )
            }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.authenticate(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addCard")
    public ResponseEntity<String> addCardToUser(@RequestBody CardDTO cardDTO, HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        customerService.addCardToUser(cardDTO, userEmail);
        return ResponseEntity.ok("Card added successfully");
    }

    //update user
    @Operation(
            description = "Update Customer Details",
            summary = "Updates Customer E-Mail, phone number and password",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Customer details updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Customer is not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal Server Error"
                    )
            }
    )
    @PutMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody UpdateCustomerDTO updateCustomerDTO, HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        customerService.updateCustomer(userEmail, updateCustomerDTO);
        return ResponseEntity.ok("Customer Details Updated Successfully");
    }

    @GetMapping("/profile-details")
    public ResponseEntity<CustomerProfile> getProfileDetails(HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        CustomerProfile customerProfile = customerService.getProfileInfo(userEmail);
        return ResponseEntity.ok().body(customerProfile);
    }

    @GetMapping("/favouriteHotels")
    public ResponseEntity<List<Hotel>> getFavouriteHotels(HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        List<Hotel> favouriteHotels = customerService.getFavouriteHotels(userEmail);
        return ResponseEntity.ok(favouriteHotels);
    }

    @PostMapping("/favouriteHotels/add")
    public ResponseEntity<String> addFavouriteHotel(@RequestParam String hotelId, HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        customerService.addFavouriteHotel(userEmail, hotelId);
        return ResponseEntity.ok("Favourite Hotel added successfully");
    }

    @DeleteMapping("/favouriteHotels/delete")
    public ResponseEntity<String> deleteFavouriteHotel(@RequestParam String hotelId, HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        customerService.deleteFavouriteHotel(userEmail, hotelId);
        return ResponseEntity.ok().body("Favourite Hotel deleted successfully");
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Hotel>> getRecentHotels(HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        List<Hotel> recentHotels = customerService.findRecentHotels(userEmail);
        return ResponseEntity.ok().body(recentHotels);
    }

    public String extractUserEmailFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Unauthorized");
        }
        final String jwtToken = authHeader.substring(7);
        return jwtService.extractUsername(jwtToken);
    }
}