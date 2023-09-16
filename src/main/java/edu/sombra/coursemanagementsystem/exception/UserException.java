package edu.sombra.coursemanagementsystem.exception;

import org.springframework.dao.DataAccessException;

public class UserException extends RuntimeException {

    public UserException(String message, DataAccessException ex) {
        super(message, ex);
    }
}