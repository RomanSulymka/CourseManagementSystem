package edu.sombra.coursemanagementsystem.exception;

import org.springframework.dao.DataAccessException;

public class CourseUpdateException extends RuntimeException {

    public CourseUpdateException(String message, DataAccessException ex) {
        super(message, ex);
    }
}