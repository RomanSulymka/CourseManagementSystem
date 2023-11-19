package edu.sombra.coursemanagementsystem.dto.course;

import edu.sombra.coursemanagementsystem.entity.Course;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;
import lombok.NonNull;

@Generated
@Data
@Builder
public class CourseDTO {
    @NonNull
    private Course course;
    @NonNull
    private String instructorEmail;
    private Long numberOfLessons;
}
