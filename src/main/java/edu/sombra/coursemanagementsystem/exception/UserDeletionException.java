package edu.sombra.coursemanagementsystem.exception;

import org.springframework.dao.DataAccessException;

public class UserDeletionException extends RuntimeException {

    public UserDeletionException(String message, DataAccessException ex) {
        super(message, ex);
    }
}