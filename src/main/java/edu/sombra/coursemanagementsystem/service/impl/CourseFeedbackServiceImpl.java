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
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.CourseFeedbackService;
import edu.sombra.coursemanagementsystem.service.UserService;
import jakarta.persistence.EntityExistsException;
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
    public static final String USER_NOT_ASSIGNED_ERROR = "User isn't assigned to this course!";
    private static final String FEEDBACK_SAVED_SUCCESSFULLY = "Feedback saved successfully";
    private static final String FAILED_TO_SAVE_FEEDBACK = "Course feedback already exists";
    private static final String COURSE_FEEDBACK_DELETED_SUCCESSFULLY = "Course Feedback deleted successfully";
    private static final String INSTRUCTOR_NOT_ASSIGNED = "Instructor is not assigned for this course";

    private final UserService userService;
    private final CourseFeedbackRepository courseFeedbackRepository;
    private final CourseRepository courseRepository;
    private final CourseFeedbackMapper courseFeedbackMapper;
    private final UserRepository userRepository;

    public GetCourseFeedbackDTO create(CourseFeedbackDTO courseFeedbackDTO, String instructorEmail) {
        try {
            User instructor = userRepository.findUserByEmail(instructorEmail);
            CourseFeedback feedback = createOrUpdateFeedback(courseFeedbackDTO, instructor);
            if (feedback.getId() != null) {
                throw new EntityExistsException(FAILED_TO_SAVE_FEEDBACK);
            }
            courseFeedbackRepository.save(feedback);
            log.info(FEEDBACK_SAVED_SUCCESSFULLY);
            return courseFeedbackMapper.mapToDTO(feedback);
        } catch (Exception e) {
            log.error(USER_NOT_ASSIGNED_ERROR);
            throw new IllegalArgumentException(USER_NOT_ASSIGNED_ERROR);
        }
    }

    @Override
    public GetCourseFeedbackDTO edit(CourseFeedbackDTO courseFeedbackDTO, String instructorEmail) {
        User instructor = userRepository.findUserByEmail(instructorEmail);
        CourseFeedback feedback = createOrUpdateFeedback(courseFeedbackDTO, instructor);
        courseFeedbackRepository.update(feedback);
        return courseFeedbackMapper.mapToDTO(feedback);
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

    public CourseFeedback findById(Long id) {
        return courseFeedbackRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public String delete(Long id) {
        CourseFeedback feedback = courseFeedbackRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
        courseFeedbackRepository.delete(feedback);
        return COURSE_FEEDBACK_DELETED_SUCCESSFULLY;
    }

    public CourseFeedback createOrUpdateFeedback(CourseFeedbackDTO courseFeedbackDTO, User instructor) {
        validateUsers(courseFeedbackDTO, instructor);
        CourseFeedback existingFeedback = null;

        if (courseFeedbackDTO.getId() != null) {
            existingFeedback = findById(courseFeedbackDTO.getId());
        }

        if (isFeedbackExist(courseFeedbackDTO.getStudentId(), courseFeedbackDTO.getCourseId())) {
            existingFeedback = findFeedback(courseFeedbackDTO.getStudentId(), courseFeedbackDTO.getCourseId());
        }

        String feedbackText = courseFeedbackDTO.getFeedbackText() != null
                ? courseFeedbackDTO.getFeedbackText()
                : (existingFeedback != null ? existingFeedback.getFeedbackText() : null);

        User student = courseFeedbackDTO.getStudentId() != null
                ? userRepository.findById(courseFeedbackDTO.getStudentId()).orElseThrow()
                : (existingFeedback != null ? existingFeedback.getStudent() : null);

        Course course = courseFeedbackDTO.getCourseId() != null
                ? courseRepository.findById(courseFeedbackDTO.getCourseId())
                .orElseThrow(EntityNotFoundException::new)
                : (existingFeedback != null ? existingFeedback.getCourse() : null);

        return CourseFeedback.builder()
                .id(existingFeedback != null ? existingFeedback.getId() : null)
                .feedbackText(feedbackText)
                .student(student)
                .instructor(instructor)
                .course(course)
                .build();
    }

    private boolean isFeedbackExist(Long studentId, Long courseId) {
        try {
            findFeedback(studentId, courseId);
            return true;
        } catch (EntityNotFoundException ex) {
            return false;
        }
    }

    private void validateUsers(CourseFeedbackDTO courseFeedbackDTO, User user) {
        if (user.getRole().equals(RoleEnum.ADMIN)) {
            courseRepository.isUserAssignedToCourse(courseFeedbackDTO.getStudentId(), courseFeedbackDTO.getCourseId());
        } else {
            if (isInstructorAssignedToCourse(user.getId(), courseFeedbackDTO.getCourseId())) {
                courseRepository.isUserAssignedToCourse(courseFeedbackDTO.getStudentId(), courseFeedbackDTO.getCourseId());
            } else {
                throw new EntityNotFoundException(INSTRUCTOR_NOT_ASSIGNED);
            }
        }
    }

    private boolean isInstructorAssignedToCourse(Long instructorId, Long courseId) {
        userService.isUserInstructor(instructorId);
        return courseRepository.isUserAssignedToCourse(instructorId, courseId);
    }
}
