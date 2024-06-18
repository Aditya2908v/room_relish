package org.example.roomrelish.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.example.roomrelish.models.Payment;
import org.example.roomrelish.services.PaymentServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

        private final PaymentServiceImpl paymentService;

        @Operation(description = "Confirm Payment process", summary = "After booking, this payment API confirms the booking by making a payment", responses = {
                @ApiResponse(description = "Details of Payment", responseCode = "200"),
                @ApiResponse(description = "No booking details found", responseCode = "204")
        })
        @PostMapping("/pay")
        public ResponseEntity<?> confirmBooking(@RequestParam String bookingId){
                Payment paymentDetails = paymentService.confirmBook(bookingId);
                return ResponseEntity.ok(paymentDetails);
        }

        @Operation(description = "Bookings of a user", summary = "This API returns the bookings that are drafted without payment", responses = {
                @ApiResponse(description = "List of details of booking", responseCode = "200"),
                @ApiResponse(description = "No bookings found", responseCode = "204")
        })
        @GetMapping("/myBookings")
        public ResponseEntity<?> myBookings(@RequestParam String userId) {
                return ResponseEntity.ok(paymentService.getMyBookings(userId));
        }

        @Operation(description = "Delete Booking", summary = "This API deletes the booking details of the room or cancels the booked room", responses = {
                @ApiResponse(description = "Deleted successful message", responseCode = "200"),
                @ApiResponse(description = "No bookings found", responseCode = "204")
        })
        @DeleteMapping("/deleteMyBooking")
        public ResponseEntity<?> deleteMyBooking(@RequestParam String bookingId) {
                return ResponseEntity.ok(paymentService.deleteBooking(bookingId));
        }
}

