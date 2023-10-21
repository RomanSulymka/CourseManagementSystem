package edu.sombra.coursemanagementsystem.exception;

import org.springframework.dao.DataAccessException;

public class LessonException extends RuntimeException {

    public LessonException(String message, DataAccessException ex) {
        super(message, ex);
    }
    public LessonException(String message) {
        super(message);
    }

    public LessonException(String message, Exception e) {
        super(message, e);
    }
}