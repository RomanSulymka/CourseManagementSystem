package edu.sombra.coursemanagementsystem.dto.enrollment;

import lombok.Data;

@Data
public class EnrollmentUpdateDTO {
    private Long id;
    private String userEmail;
    private String courseName;
}
