package org.example.roomrelish.ExceptionHandler;

public class CustomNoBookingFoundException extends Exception {
    public CustomNoBookingFoundException(String errorMessage){
        super(errorMessage);
    }
}
