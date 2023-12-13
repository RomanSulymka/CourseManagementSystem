package edu.sombra.coursemanagementsystem.exception;

public class UserCreationException extends RuntimeException {

    public UserCreationException(String message, Exception ex) {
        super(message, ex);
    }

    public UserCreationException(Exception e) {
        super((e));
    }
}