package edu.sombra.coursemanagementsystem.exception;

import lombok.Generated;

@Generated
public class UserCreationException extends RuntimeException {

    public UserCreationException(String message, UserCreationException ex) {
        super(message, ex);
    }
}