package org.example.roomrelish.ExceptionHandler;

public class CustomNoPaymentFoundException extends Exception {
    public CustomNoPaymentFoundException(String errorMessage){
        super(errorMessage);
    }

}
