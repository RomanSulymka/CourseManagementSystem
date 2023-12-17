package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.feedback.CourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.dto.feedback.GetCourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.entity.CourseFeedback;

import java.util.List;

public interface CourseFeedbackService {
    GetCourseFeedbackDTO create(CourseFeedbackDTO courseFeedbackDTO, String instructorEmail);

    CourseFeedback findFeedback(Long studentId, Long courseId);

    List<GetCourseFeedbackDTO> findAll();

    GetCourseFeedbackDTO findCourseFeedbackById(Long id, String userEmail);

    String delete(Long id);

    GetCourseFeedbackDTO edit(CourseFeedbackDTO courseFeedbackDTO, String instructorEmail);
}
