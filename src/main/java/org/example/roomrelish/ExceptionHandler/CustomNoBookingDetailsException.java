package org.example.roomrelish.ExceptionHandler;

public class CustomNoBookingDetailsException extends Exception {
    public CustomNoBookingDetailsException(String errorMessage){
        super(errorMessage);
    }

}
