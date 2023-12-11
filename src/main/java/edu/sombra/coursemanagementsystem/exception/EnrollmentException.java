package edu.sombra.coursemanagementsystem.exception;

public class EnrollmentException extends RuntimeException {

    public EnrollmentException(String message) {
        super(message);
    }

    public EnrollmentException(String message, RuntimeException ex) {
        super(message, ex);
    }

    public EnrollmentException() {

    }
}
