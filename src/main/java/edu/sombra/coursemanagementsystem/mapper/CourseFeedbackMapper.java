package edu.sombra.coursemanagementsystem.mapper;

import edu.sombra.coursemanagementsystem.dto.feedback.CourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.entity.CourseFeedback;
import edu.sombra.coursemanagementsystem.service.CourseService;
import edu.sombra.coursemanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CourseFeedbackMapper {
    private final UserService userService;
    private final CourseService courseService;

    public CourseFeedback mapFromDTO(CourseFeedbackDTO feedbackDTO) {
        return CourseFeedback.builder()
                .feedbackText(feedbackDTO.getFeedbackText())
                .course(courseService.findById(feedbackDTO.getCourseId()))
                .instructor(userService.findUserById(feedbackDTO.getInstructorId()))
                .student(userService.findUserById(feedbackDTO.getStudentId()))
                .build();
    }
}
