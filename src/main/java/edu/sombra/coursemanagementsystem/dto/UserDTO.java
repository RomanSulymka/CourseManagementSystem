package edu.sombra.coursemanagementsystem.dto;

import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import lombok.Data;

@Data
public class UserDTO {
    private String email;
    private RoleEnum role;
}
