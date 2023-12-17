package edu.sombra.coursemanagementsystem.dto.course;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseAssignedToUserDTO {
    private Long courseId;
    private Long userId;
}
