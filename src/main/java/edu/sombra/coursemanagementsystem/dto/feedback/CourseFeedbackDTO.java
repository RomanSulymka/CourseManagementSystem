package edu.sombra.coursemanagementsystem.dto.feedback;

import lombok.Data;
import lombok.Generated;

@Generated
@Data
public class CourseFeedbackDTO {
    private Long id;
    private String feedbackText;
    private Long courseId;
    private Long studentId;
}
