package edu.sombra.coursemanagementsystem.dto.enrollment;

import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Data
@Builder
public class EnrollmentGetDTO {
    private String courseName;
    private String userEmail;
    private RoleEnum role;
}
