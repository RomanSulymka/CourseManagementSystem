package edu.sombra.coursemanagementsystem.exception;

import jakarta.persistence.NoResultException;
import org.springframework.dao.DataAccessException;

public class EnrollmentException extends RuntimeException {

    public EnrollmentException(String message) {
        super(message);
    }

    public EnrollmentException(String message, DataAccessException ex) {
        super(message, ex);
    }

    public EnrollmentException(String message, NoResultException ex) {
        super(message, ex);
    }
}