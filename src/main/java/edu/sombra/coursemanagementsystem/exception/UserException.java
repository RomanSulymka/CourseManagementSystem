package edu.sombra.coursemanagementsystem.exception;

import lombok.Generated;

@Generated
public class UserException extends RuntimeException {

    public UserException(String message, RuntimeException ex) {
        super(message, ex);
    }

    public UserException(String message) {
        super(message);
    }
}