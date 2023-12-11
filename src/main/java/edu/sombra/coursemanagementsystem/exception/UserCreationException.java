package edu.sombra.coursemanagementsystem.exception;

public class UserCreationException extends RuntimeException {

    public UserCreationException(String message, UserCreationException ex) {
        super(message, ex);
    }
}