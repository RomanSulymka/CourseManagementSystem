package edu.sombra.coursemanagementsystem.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Generated
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseFeedbackDTO {
    private Long id;
    private String feedbackText;
    private Long courseId;
    private Long studentId;
}
