package org.example.roomrelish.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.roomrelish.dto.*;
import org.example.roomrelish.models.Customer;
import org.example.roomrelish.models.Hotel;
import org.example.roomrelish.services.AuthService;
import org.example.roomrelish.services.CustomerService;
import org.example.roomrelish.services.JwtService;
import org.jetbrains.annotations.TestOnly;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final AuthService authService;
    private final JwtService jwtService;
    String errorMessageCustomer = "Customer not found";


    @QueryMapping("users")
    @TestOnly
    public List<Customer> getAllCustomersGraphQL() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers() {
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
    public ResponseEntity<?> registerCustomer(@RequestBody RegisterUserDTO request) {
        RegisterRequest registerRequest = authService.getRegisterRequest(request);
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
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.authenticate(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addCard")
    public ResponseEntity<?> addCardToUser(@RequestBody CardDTO cardDTO, HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        customerService.addCardToUser(cardDTO, userEmail);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<?> updateUser(@RequestBody UpdateCustomerDTO updateCustomerDTO, HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        if (customerService.updateCustomer(userEmail, updateCustomerDTO)) {
            return ResponseEntity.ok().body("Customer successfully updated");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessageCustomer);
    }

    //specifically for navbar/ to check weather the user is logged in
    @GetMapping("/navbar")
    public ResponseEntity<?> getNavbarDetails(HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        if (userEmail == null || userEmail.isEmpty()) {
            return ResponseEntity.ok().body(new NavbarResponse(false, null));
        }
        CustomerProfile customerProfile = customerService.getProfileInfo(userEmail, "navbar");
        if (customerProfile == null) {
            return ResponseEntity.badRequest().body(errorMessageCustomer);
        }
        return ResponseEntity.ok().body(new NavbarResponse(true, customerProfile));
    }

    //get profile details for profile page
    @GetMapping("/profile-details")
    public ResponseEntity<?> getProfileDetails(HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        CustomerProfile customerProfile = customerService.getProfileInfo(userEmail, "profile");
        if (customerProfile == null) {
            return ResponseEntity.badRequest().body(errorMessageCustomer);
        }
        return ResponseEntity.ok().body(customerProfile);
    }

    //get profile picture
    @GetMapping("/profilePicture")
    public ResponseEntity<?> getProfilePicture(HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        String profilePicture = customerService.getProfilePicture(userEmail);
        return ResponseEntity.ok().body(profilePicture);
    }

    // add or update profile or cover picture
    @PostMapping("/addProfilePicture")
    public ResponseEntity<?> addOrUpdateProfilePhoto(@RequestParam("file") MultipartFile file, @RequestParam String type, HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid image format");
        }
        boolean success = customerService.uploadImage(userEmail, type, fileName);
        if (success) {
            return ResponseEntity.ok("Image uploaded successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request");
        }
    }

    // to view list of favourite hotels
    @GetMapping("/favouriteHotels")
    public ResponseEntity<?> getFavouriteHotels(HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        List<Hotel> favouriteHotels = customerService.getFavouriteHotels(userEmail);
        if (favouriteHotels.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hotels not found");
        else return ResponseEntity.ok(favouriteHotels);
    }

    // to add a hotel to favourite hotels if it is present in hotel collection
    @PostMapping("/favouriteHotels/add")
    public ResponseEntity<?> addFavouriteHotel(@RequestParam String hotelId, HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        customerService.addFavouriteHotel(userEmail, hotelId);
        return ResponseEntity.ok().body("Favourite Hotel added successfully");
    }

    // to remove a hotel from favourite hotels
    @DeleteMapping("/favouriteHotels/delete")
    public ResponseEntity<?> deleteFavouriteHotel(@RequestParam String hotelId, HttpServletRequest request) {
        String userEmail = extractUserEmailFromRequest(request);
        customerService.deleteFavouriteHotel(userEmail, hotelId);
        return ResponseEntity.ok().body("Favourite Hotel deleted successfully");
    }

    // recent searches
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentHotels(HttpServletRequest request) {
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