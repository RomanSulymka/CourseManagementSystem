package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.feedback.CourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.entity.CourseFeedback;
import edu.sombra.coursemanagementsystem.mapper.CourseFeedbackMapper;
import edu.sombra.coursemanagementsystem.repository.CourseFeedbackRepository;
import edu.sombra.coursemanagementsystem.service.CourseFeedbackService;
import edu.sombra.coursemanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class CourseFeedbackServiceImpl implements CourseFeedbackService {
    private final UserService userService;
    private final CourseFeedbackRepository courseFeedbackRepository;
    private final CourseFeedbackMapper courseFeedbackMapper;

    @Override
    public String create(CourseFeedbackDTO courseFeedbackDTO) {
        try {
            validateUsers(courseFeedbackDTO);
            CourseFeedback courseFeedback = courseFeedbackMapper.mapFromDTO(courseFeedbackDTO);
            courseFeedbackRepository.save(courseFeedback);
            log.info("Feedback saved successfully!");
            return "Feedback saved successfully!";
        } catch (Exception e) {
            log.error("Failed to save the feedback for user {} and course {}", courseFeedbackDTO.getStudentId(), courseFeedbackDTO.getCourseId());
            throw new IllegalArgumentException("Failed to save the feedback!");
        }
    }

    @Override
    public CourseFeedback findFeedback(Long studentId, Long courseId) {
        return courseFeedbackRepository.findFeedback(studentId, courseId)
                .orElse(null);
    }

    private void validateUsers(CourseFeedbackDTO courseFeedbackDTO) {
        userService.isInstructorAssignedToCourse(courseFeedbackDTO.getInstructorId(), courseFeedbackDTO.getCourseId());
        userService.isStudentAssignedToCourse(courseFeedbackDTO.getStudentId(), courseFeedbackDTO.getCourseId());
    }
}
