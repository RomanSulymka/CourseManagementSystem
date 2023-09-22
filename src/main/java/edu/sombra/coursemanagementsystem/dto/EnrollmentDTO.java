package edu.sombra.coursemanagementsystem.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
public class EnrollmentDTO {
    @NonNull
    private String userEmail;
    @NonNull
    private String courseName;
}
