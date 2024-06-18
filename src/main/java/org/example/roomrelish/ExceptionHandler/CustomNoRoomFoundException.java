package org.example.roomrelish.ExceptionHandler;

public class CustomNoRoomFoundException extends Exception{
    public CustomNoRoomFoundException(String message){
        super(message);
    }
}
