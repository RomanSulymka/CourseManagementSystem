package edu.sombra.coursemanagementsystem.dto.enrollment;

import lombok.Data;

@Data
public class RemoveInstructorDTO {
    private String instructorEmail;
    private String courseName;
}
