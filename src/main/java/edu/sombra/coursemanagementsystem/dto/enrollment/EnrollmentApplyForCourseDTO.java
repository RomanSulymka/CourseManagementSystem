package edu.sombra.coursemanagementsystem.dto.enrollment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnrollmentApplyForCourseDTO {
    private String courseName;
    private Long userId;
}
