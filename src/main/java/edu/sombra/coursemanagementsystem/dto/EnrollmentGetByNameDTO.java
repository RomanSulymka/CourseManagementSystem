package edu.sombra.coursemanagementsystem.dto;

import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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
