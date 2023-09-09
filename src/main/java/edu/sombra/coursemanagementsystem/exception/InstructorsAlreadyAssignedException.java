package edu.sombra.coursemanagementsystem.exception;

public class InstructorsAlreadyAssignedException extends RuntimeException {
    public InstructorsAlreadyAssignedException(String message) {
        super(message);
    }
}
