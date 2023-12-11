package edu.sombra.coursemanagementsystem.exception;

import jakarta.persistence.EntityNotFoundException;

public class EntityDeletionException extends RuntimeException {

    public EntityDeletionException(String message, EntityNotFoundException e) {
        super(message, e);
    }

    public EntityDeletionException(String message, EntityDeletionException e) {
        super(message, e);
    }
}
