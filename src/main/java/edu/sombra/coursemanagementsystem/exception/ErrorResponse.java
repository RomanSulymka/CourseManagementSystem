package edu.sombra.coursemanagementsystem.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;

@Generated
@Data
@AllArgsConstructor
public class ErrorResponse {
    private int statusCode;
    private String message;

}