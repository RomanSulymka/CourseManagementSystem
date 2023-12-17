package edu.sombra.coursemanagementsystem.dto.enrollment;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Data
@Builder
public class EnrollmentApplyForCourseDTO {
    private String courseName;
    private Long userId;
}
