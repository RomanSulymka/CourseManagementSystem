package edu.sombra.coursemanagementsystem.dto.user;

import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDTO {
    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private RoleEnum role;
}
