package edu.sombra.coursemanagementsystem.dto.user;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Data
@Builder
public class ResetPasswordDTO {
    private String email;
    private String newPassword;
}
