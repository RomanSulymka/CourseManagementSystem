package edu.sombra.coursemanagementsystem.exception;

import lombok.Generated;

@Generated
public class UserUpdateException extends RuntimeException {

    public UserUpdateException(String message, NullPointerException ex) {
        super(message, ex);
    }
}