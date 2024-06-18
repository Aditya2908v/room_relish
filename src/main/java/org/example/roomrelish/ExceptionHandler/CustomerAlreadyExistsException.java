package org.example.roomrelish.ExceptionHandler;

import org.example.roomrelish.models.Customer;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CustomerAlreadyExistsException extends RuntimeException{
    public CustomerAlreadyExistsException(String resourceName, String fieldName, String fieldValue){
        super(String.format("%s already exists with the given input data %s : '%s'",resourceName, fieldName, fieldValue));
    }
}
