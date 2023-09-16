package edu.sombra.coursemanagementsystem.dto;

import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnrollmentGetDTO {
    private String courseName;
    private String userEmail;
    private RoleEnum role;
}
