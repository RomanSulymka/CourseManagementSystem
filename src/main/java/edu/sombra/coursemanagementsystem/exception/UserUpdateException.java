package edu.sombra.coursemanagementsystem.exception;

public class UserUpdateException extends RuntimeException {

    public UserUpdateException(String message, Exception ex) {
        super(message, ex);
    }
}