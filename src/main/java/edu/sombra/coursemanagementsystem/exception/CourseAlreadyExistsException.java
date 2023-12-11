package edu.sombra.coursemanagementsystem.exception;

public class CourseAlreadyExistsException extends RuntimeException {
    public CourseAlreadyExistsException(String email) {
        super("Course with this name is already exist: " + email);
    }
}
