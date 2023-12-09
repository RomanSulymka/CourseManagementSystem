package edu.sombra.coursemanagementsystem.mapper;

import edu.sombra.coursemanagementsystem.dto.feedback.GetCourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.entity.CourseFeedback;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseFeedbackMapper {

    public List<GetCourseFeedbackDTO> mapToDTO(List<CourseFeedback> feedbackList) {
        return feedbackList.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public GetCourseFeedbackDTO mapToDTO(CourseFeedback feedback) {
        return GetCourseFeedbackDTO.builder()
                .id(feedback.getId())
                .feedbackText(feedback.getFeedbackText())
                .studentId(feedback.getStudent().getId())
                .studentEmail(feedback.getStudent().getEmail())
                .instructorId(feedback.getInstructor().getId())
                .instructorEmail(feedback.getInstructor().getEmail())
                .course(feedback.getCourse())
                .build();
    }
}
