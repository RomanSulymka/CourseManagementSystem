package edu.sombra.coursemanagementsystem.exception;

public class UserException extends RuntimeException {

    public UserException(String message) {
        super(message);
    }

    public UserException(String failedToCreateUser, Exception ex) {
        super(failedToCreateUser, ex);
    }
}