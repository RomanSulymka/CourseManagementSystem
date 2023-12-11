package edu.sombra.coursemanagementsystem.dto.enrollment;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;
import lombok.NonNull;

@Builder
@Data
public class EnrollmentDTO {
    @NonNull
    private String userEmail;
    @NonNull
    private String courseName;
}
