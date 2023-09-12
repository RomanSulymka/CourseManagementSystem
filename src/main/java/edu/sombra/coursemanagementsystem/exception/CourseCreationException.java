package edu.sombra.coursemanagementsystem.exception;

import org.springframework.dao.DataAccessException;

public class CourseCreationException extends RuntimeException {

    public CourseCreationException(String message, DataAccessException ex) {
        super(message, ex);
    }
}