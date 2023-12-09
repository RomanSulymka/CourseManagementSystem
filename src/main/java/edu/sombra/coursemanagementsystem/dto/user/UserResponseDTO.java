package edu.sombra.coursemanagementsystem.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private RoleEnum role;

    @JsonIgnore
    private String password;
}
