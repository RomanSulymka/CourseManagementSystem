package edu.sombra.coursemanagementsystem.dto;

import lombok.Data;

@Data
public class EnrollmentUpdateDTO {
    private Long id;
    private String userEmail;
    private String courseName;
}
