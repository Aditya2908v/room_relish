package org.example.roomrelish.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class RoomUnavailableException extends RuntimeException {
    public RoomUnavailableException(String msg) {
        super(msg);
    }
}
