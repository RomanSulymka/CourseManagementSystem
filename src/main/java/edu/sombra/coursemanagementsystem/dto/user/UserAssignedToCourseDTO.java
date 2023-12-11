package edu.sombra.coursemanagementsystem.dto.user;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Builder
@Data
public class UserAssignedToCourseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
