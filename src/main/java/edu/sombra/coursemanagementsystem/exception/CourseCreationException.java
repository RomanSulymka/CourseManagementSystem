package edu.sombra.coursemanagementsystem.exception;

public class CourseCreationException extends RuntimeException {

    public CourseCreationException(String message) {
        super(message);
    }

    public CourseCreationException(Exception message) {
        super(message);
    }
}
