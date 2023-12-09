package edu.sombra.coursemanagementsystem.exception;

import lombok.Generated;

@Generated
public class CourseAlreadyExistsException extends RuntimeException {
    public CourseAlreadyExistsException(String email) {
        super("Course with this name is already exist: " + email);
    }
}
