package edu.sombra.coursemanagementsystem.dto.feedback;

import lombok.Data;

@Data
public class CourseFeedbackDTO {
    private Long id;
    private String feedbackText;
    private Long courseId;
    private Long studentId;
}
