package edu.sombra.coursemanagementsystem.exception;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(Long id) {
    super("Could not find User with id: " + id);
  }

  public UserNotFoundException(String message) {
    super(message);
  }
}