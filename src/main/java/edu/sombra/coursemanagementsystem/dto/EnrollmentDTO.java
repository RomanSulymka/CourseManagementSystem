package edu.sombra.coursemanagementsystem.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class EnrollmentDTO {
    @NonNull
    private String instructorEmail;
    @NonNull
    private String courseName;
}
