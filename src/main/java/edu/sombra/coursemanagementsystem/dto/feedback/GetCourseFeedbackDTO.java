package edu.sombra.coursemanagementsystem.dto.feedback;

import edu.sombra.coursemanagementsystem.entity.Course;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Builder
@Data
public class GetCourseFeedbackDTO {
    private Long id;
    private String feedbackText;
    private Course course;
    private Long instructorId;
    private String instructorEmail;
    private Long studentId;
    private String studentEmail;
}
