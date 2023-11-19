package edu.sombra.coursemanagementsystem.exception;

import jakarta.persistence.NoResultException;
import lombok.Generated;

@Generated
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