package edu.sombra.coursemanagementsystem.dto;

import edu.sombra.coursemanagementsystem.entity.Course;
import lombok.Data;
import lombok.NonNull;

@Data
public class CourseDTO {
    @NonNull
    private Course course;
    @NonNull
    private String instructorEmail;
}
