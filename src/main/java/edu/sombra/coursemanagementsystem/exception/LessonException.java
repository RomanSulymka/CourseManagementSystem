package edu.sombra.coursemanagementsystem.exception;

public class LessonException extends RuntimeException {
    public LessonException(String message) {
        super(message);
    }

    public LessonException(String message, Exception e) {
        super(message, e);
    }
}
