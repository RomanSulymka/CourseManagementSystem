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
import edu.sombra.coursemanagementsystem.repository.EnrollmentRepository;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.CourseFeedbackService;
import edu.sombra.coursemanagementsystem.service.UserService;
import edu.sombra.coursemanagementsystem.util.BaseUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class CourseFeedbackServiceImpl implements CourseFeedbackService {
    private static final String INSTRUCTOR_NOT_ASSIGNED = "Instructor is not assigned for this course";
    public static final String FEEDBACK_IS_NOT_VISIBLE_FOR_THIS_USER = "Course feedback is not visible for this user!";

    private final UserService userService;
    private final CourseFeedbackRepository courseFeedbackRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final CourseFeedbackMapper courseFeedbackMapper;
    private final UserRepository userRepository;

    public GetCourseFeedbackDTO create(CourseFeedbackDTO courseFeedbackDTO, String instructorEmail) {
        try {
            if (!isFeedbackExist(courseFeedbackDTO.getStudentId(), courseFeedbackDTO.getCourseId())) {
                User instructor = userRepository.findUserByEmail(instructorEmail);
                CourseFeedback feedback = createFeedback(courseFeedbackDTO, instructor);
                log.info("Feedback saved successfully");
                return courseFeedbackMapper.mapToDTO(feedback);
            }
            throw new IllegalArgumentException("Feedback already exist!");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException("Failed to create feedback: " + e.getMessage());
        }
    }

    private boolean isFeedbackExist(Long studentId, Long courseId) {
        Optional<CourseFeedback> feedbackOptional = courseFeedbackRepository.findFeedback(studentId, courseId);
        return feedbackOptional.isPresent();
    }

    @Override
    public GetCourseFeedbackDTO edit(CourseFeedbackDTO courseFeedbackDTO, String instructorEmail) {
        try {
            CourseFeedback existingFeedback = courseFeedbackRepository.findById(courseFeedbackDTO.getId()).orElseThrow();
            BeanUtils.copyProperties(courseFeedbackDTO, existingFeedback, BaseUtil.getNullPropertyNames(courseFeedbackDTO));
            courseFeedbackRepository.update(existingFeedback);
            GetCourseFeedbackDTO updatedFeedback = courseFeedbackMapper.mapToDTO(existingFeedback);
            log.info("Course feedback successfully updated!");
            return updatedFeedback;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException("Failed to update feedback!");
        }
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
    public GetCourseFeedbackDTO findCourseFeedbackById(Long id, String userEmail) {
        User user = userRepository.findUserByEmail(userEmail);
        CourseFeedback feedback = findById(id);

        if (canUserAccessFeedback(user, feedback)) {
            return courseFeedbackMapper.mapToDTO(feedback);
        } else {
            log.error(FEEDBACK_IS_NOT_VISIBLE_FOR_THIS_USER);
            throw new IllegalArgumentException(FEEDBACK_IS_NOT_VISIBLE_FOR_THIS_USER);
        }
    }

    private boolean canUserAccessFeedback(User user, CourseFeedback feedback) {
        RoleEnum userRole = user.getRole();

        if (userRole.equals(RoleEnum.ADMIN)) {
            return true;
        } else if (userRole.equals(RoleEnum.INSTRUCTOR)) {
            return enrollmentRepository.isUserAssignedToCourse(feedback.getCourse(), user);
        } else {
            return feedback.getStudent().getId().equals(user.getId());
        }
    }

    public CourseFeedback findById(Long id) {
        return courseFeedbackRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public void delete(Long id) {
        CourseFeedback feedback = courseFeedbackRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
        courseFeedbackRepository.delete(feedback);
    }

    public CourseFeedback createFeedback(CourseFeedbackDTO courseFeedbackDTO, User instructor) {
        validateUsers(courseFeedbackDTO, instructor);

        User student = userRepository.findById(courseFeedbackDTO.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found!"));

        Course course = courseRepository.findById(courseFeedbackDTO.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found!"));

        CourseFeedback feedback = CourseFeedback.builder()
                .feedbackText(courseFeedbackDTO.getFeedbackText())
                .student(student)
                .instructor(instructor)
                .course(course)
                .build();

        return courseFeedbackRepository.save(feedback);
    }

    private void validateUsers(CourseFeedbackDTO courseFeedbackDTO, User user) {
        if (user.getRole().equals(RoleEnum.ADMIN)) {
            courseRepository.isUserAssignedToCourse(courseFeedbackDTO.getStudentId(), courseFeedbackDTO.getCourseId());
        } else {
            if (isInstructorAssignedToCourse(user.getId(), courseFeedbackDTO.getCourseId())) {
                courseRepository.isUserAssignedToCourse(courseFeedbackDTO.getStudentId(), courseFeedbackDTO.getCourseId());
            } else {
                log.error(INSTRUCTOR_NOT_ASSIGNED);
                throw new EntityNotFoundException(INSTRUCTOR_NOT_ASSIGNED);
            }
        }
    }

    private boolean isInstructorAssignedToCourse(Long instructorId, Long courseId) {
        userService.isUserInstructor(instructorId);
        return courseRepository.isUserAssignedToCourse(instructorId, courseId);
    }
}
