package edu.sombra.coursemanagementsystem.dto.feedback;

import edu.sombra.coursemanagementsystem.entity.Course;
import lombok.Data;
import lombok.NonNull;

@Data
public class CourseFeedbackDTO {
    private String feedbackText;
    private Long courseId;
    private Long instructorId;
    private Long studentId;
}
