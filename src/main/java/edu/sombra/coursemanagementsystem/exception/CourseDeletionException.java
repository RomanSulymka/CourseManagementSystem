package edu.sombra.coursemanagementsystem.exception;

import org.springframework.dao.DataAccessException;

public class CourseDeletionException extends RuntimeException {

    public CourseDeletionException(String message, DataAccessException ex) {
        super(message, ex);
    }
}