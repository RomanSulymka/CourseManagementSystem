package edu.sombra.coursemanagementsystem.dto.user;

import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO {
    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private RoleEnum role;
}
