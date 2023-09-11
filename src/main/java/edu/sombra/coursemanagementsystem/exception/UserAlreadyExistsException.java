package edu.sombra.coursemanagementsystem.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("User with this email is already exist " + email);
    }
}
