package edu.sombra.coursemanagementsystem.dto;

import lombok.Data;

@Data
public class ResetPasswordDTO {
    private String email;
    private String newPassword;
}
