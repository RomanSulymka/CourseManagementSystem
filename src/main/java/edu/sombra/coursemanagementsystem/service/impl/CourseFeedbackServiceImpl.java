package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.feedback.CourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.dto.feedback.GetCourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.CourseFeedback;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.mapper.CourseFeedbackMapper;
import edu.sombra.coursemanagementsystem.repository.CourseFeedbackRepository;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.service.CourseFeedbackService;
import edu.sombra.coursemanagementsystem.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class CourseFeedbackServiceImpl implements CourseFeedbackService {
    private final UserService userService;
    private final CourseFeedbackRepository courseFeedbackRepository;
    private final CourseRepository courseRepository;
    private final CourseFeedbackMapper courseFeedbackMapper;

    @Override
    public String create(CourseFeedbackDTO courseFeedbackDTO, String instructorEmail) {
        try {
            Course course = courseRepository.findById(courseFeedbackDTO.getCourseId())
                    .orElseThrow(EntityNotFoundException::new);
            User instructor = userService.findUserByEmail(instructorEmail);
            User student = userService.findUserById(courseFeedbackDTO.getStudentId());
            validateUsers(courseFeedbackDTO, instructor);
            CourseFeedback courseFeedback = courseFeedbackMapper.mapFromDTO(courseFeedbackDTO, course,
                    instructor, student);
            courseFeedbackRepository.save(courseFeedback);
            log.info("Feedback saved successfully!");
            return "Feedback saved successfully!";
        } catch (Exception e) {
            log.error("Failed to save the feedback for user {} and course {}",
                    courseFeedbackDTO.getStudentId(), courseFeedbackDTO.getCourseId());
            throw new IllegalArgumentException("Failed to save the feedback!");
        }
    }

    @Override
    public GetCourseFeedbackDTO edit(CourseFeedbackDTO courseFeedbackDTO, String instructorEmail) {
        User instructor = userService.findUserByEmail(instructorEmail);
        CourseFeedback feedback = validateCourseFeedback(courseFeedbackDTO, instructor);
        courseFeedbackRepository.update(feedback);
        return courseFeedbackMapper.mapToDTO(feedback);
    }

    private CourseFeedback validateCourseFeedback(CourseFeedbackDTO courseFeedbackDTO, User instructor) {
        CourseFeedback existingFeedback = findById(courseFeedbackDTO.getId());

        String feedbackText = courseFeedbackDTO.getFeedbackText() != null
                ? courseFeedbackDTO.getFeedbackText()
                : existingFeedback.getFeedbackText();

        User student = courseFeedbackDTO.getStudentId() != null
                ? userService.findUserById(courseFeedbackDTO.getStudentId())
                : existingFeedback.getStudent();

        Course course = courseFeedbackDTO.getCourseId() != null
                ? courseRepository.findById(courseFeedbackDTO.getCourseId())
                .orElseThrow(EntityNotFoundException::new)
                : existingFeedback.getCourse();

        return CourseFeedback.builder()
                .id(existingFeedback.getId())
                .feedbackText(feedbackText)
                .student(student)
                .instructor(instructor)
                .course(course)
                .build();
    }

    @Override
    public CourseFeedback findFeedback(Long studentId, Long courseId) {
        return courseFeedbackRepository.findFeedback(studentId, courseId)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public List<GetCourseFeedbackDTO> findAll() {
        List<CourseFeedback> feedbackList = courseFeedbackRepository.findAll();
        return courseFeedbackMapper.mapToDTO(feedbackList);
    }

    @Override
    public GetCourseFeedbackDTO findCourseFeedbackById(Long id) {
        CourseFeedback feedback = findById(id);
        return courseFeedbackMapper.mapToDTO(feedback);
    }

    private CourseFeedback findById(Long id) {
        return courseFeedbackRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public String delete(Long id) {
        CourseFeedback feedback = courseFeedbackRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
        courseFeedbackRepository.delete(feedback);
        return "Course Feedback deleted successfully";
    }

    private void validateUsers(CourseFeedbackDTO courseFeedbackDTO, User instructor) {
        if (instructor.getRole().equals(RoleEnum.ADMIN)) {
            courseRepository.isUserAssignedToCourse(courseFeedbackDTO.getStudentId(), courseFeedbackDTO.getCourseId());
        } else {
            if (isInstructorAssignedToCourse(instructor.getId(), courseFeedbackDTO.getCourseId())) {
                courseRepository.isUserAssignedToCourse(courseFeedbackDTO.getStudentId(), courseFeedbackDTO.getCourseId());
            } else {
                throw new EntityNotFoundException("Instructor is not assigned fot this course!");
            }
        }
    }

    private boolean isInstructorAssignedToCourse(Long instructorId, Long courseId) {
        userService.isUserInstructor(instructorId);
        return courseRepository.isUserAssignedToCourse(instructorId, courseId);
    }
}
