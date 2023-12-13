package edu.sombra.coursemanagementsystem.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResetPasswordDTO {
    private Long id;
    private String email;
    private String newPassword;
}
