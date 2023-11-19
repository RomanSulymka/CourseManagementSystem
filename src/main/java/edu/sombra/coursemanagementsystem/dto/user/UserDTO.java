package edu.sombra.coursemanagementsystem.dto.user;

import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
@Builder
public class UserDTO {
    private String email;
    private RoleEnum role;
}
