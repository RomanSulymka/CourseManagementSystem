package edu.sombra.coursemanagementsystem.exception;

import org.springframework.dao.DataAccessException;

public class CourseStartDateExpired extends RuntimeException {

    public CourseStartDateExpired(String message) {
        super(message);
    }
}