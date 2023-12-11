package edu.sombra.coursemanagementsystem.exception;

public class UserException extends RuntimeException {

    public UserException(String message, RuntimeException ex) {
        super(message, ex);
    }

    public UserException(String message) {
        super(message);
    }
}