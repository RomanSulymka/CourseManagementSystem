package edu.sombra.coursemanagementsystem.exception;

public class UserNotAssignedToCourseException extends RuntimeException {

    public UserNotAssignedToCourseException(String message) {
        super(message);
    }
}