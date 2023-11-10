package edu.sombra.coursemanagementsystem.exception;

public class CourseCreationException extends RuntimeException {

    public CourseCreationException(String message, RuntimeException ex) {
        super(message, ex);
    }

    public CourseCreationException(String message) {
        super(message);
    }
}