package edu.sombra.coursemanagementsystem.dto.enrollment;

import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnrollmentResponseDTO {
    private Long enrollmentId;
    private Long courseId;
    private String courseName;
    private Long userId;
    private String userEmail;
    private RoleEnum role;
}
