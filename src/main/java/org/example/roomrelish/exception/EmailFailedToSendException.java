package org.example.roomrelish.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class EmailFailedToSendException extends RuntimeException{
    public EmailFailedToSendException(){
        super("Email failed to send");
    }
}
