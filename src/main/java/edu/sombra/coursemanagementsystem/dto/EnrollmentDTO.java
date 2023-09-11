package edu.sombra.coursemanagementsystem.dto;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class EnrollmentDTO {
    @NonNull
    private List<String> instructorsEmail;
    @NonNull
    private String courseName;
}
