package edu.sombra.coursemanagementsystem.exception;

public class UserUpdateException extends RuntimeException {

    public UserUpdateException(String message, NullPointerException ex) {
        super(message, ex);
    }
}