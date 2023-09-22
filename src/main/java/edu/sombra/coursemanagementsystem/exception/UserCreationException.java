package edu.sombra.coursemanagementsystem.exception;

import org.springframework.dao.DataAccessException;

public class UserCreationException extends RuntimeException {

    public UserCreationException(String message, DataAccessException ex) {
        super(message, ex);
    }
}