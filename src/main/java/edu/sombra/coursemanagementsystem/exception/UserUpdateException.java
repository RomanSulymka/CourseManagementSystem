package edu.sombra.coursemanagementsystem.exception;

import org.springframework.dao.DataAccessException;

public class UserUpdateException extends RuntimeException {

    public UserUpdateException(String message, DataAccessException ex) {
        super(message, ex);
    }
}