package edu.sombra.coursemanagementsystem.exception;

import org.springframework.dao.DataAccessException;

public class EntityDeletionException extends RuntimeException {

    public EntityDeletionException(String message, DataAccessException ex) {
        super(message, ex);
    }
    public EntityDeletionException(String message) {
        super(message);
    }
}