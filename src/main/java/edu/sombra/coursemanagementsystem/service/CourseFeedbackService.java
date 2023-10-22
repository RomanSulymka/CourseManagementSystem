package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.feedback.CourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.entity.CourseFeedback;

public interface CourseFeedbackService {
    String create(CourseFeedbackDTO courseFeedbackDTO);

    CourseFeedback findFeedback(Long studentId, Long courseId);
}
