package edu.sombra.coursemanagementsystem.exception;

import lombok.Generated;

@Generated
public class UserAlreadyAssignedException extends RuntimeException {
    public UserAlreadyAssignedException(String message) {
        super(message);
    }
}
