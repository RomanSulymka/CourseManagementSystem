package edu.sombra.coursemanagementsystem.exception;

import org.springframework.dao.DataAccessException;

public class EnrollmentException extends RuntimeException {

    public EnrollmentException(String message) {
        super(message);
    }

    public EnrollmentException(String message, DataAccessException ex) {
        super(message, ex);
    }

}