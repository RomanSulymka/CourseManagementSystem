package edu.sombra.coursemanagementsystem.dto.enrollment;

import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Data
@Builder
@AllArgsConstructor
public class EnrollmentGetByNameDTO {
    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private RoleEnum role;
}
