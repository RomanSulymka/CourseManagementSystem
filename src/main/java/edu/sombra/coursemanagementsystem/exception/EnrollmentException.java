package edu.sombra.coursemanagementsystem.exception;

import jakarta.persistence.NoResultException;

public class EnrollmentException extends RuntimeException {

    public EnrollmentException(String message) {
        super(message);
    }

    public EnrollmentException(String message, RuntimeException ex) {
        super(message, ex);
    }

    public EnrollmentException(String message, NoResultException ex) {
        super(message, ex);
    }

    public EnrollmentException() {

    }
}