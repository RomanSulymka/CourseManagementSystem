package edu.sombra.coursemanagementsystem.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.Generated;

@Generated
public class EntityDeletionException extends RuntimeException {

    public EntityDeletionException(String message, EntityNotFoundException e) {
        super(message, e);
    }

    public EntityDeletionException(String message, EntityDeletionException e) {
        super(message, e);
    }

    public EntityDeletionException(String message, NullPointerException ex) {
        super(message, ex);
    }
}