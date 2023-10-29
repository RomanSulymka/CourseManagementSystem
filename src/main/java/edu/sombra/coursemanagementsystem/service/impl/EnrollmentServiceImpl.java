package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentApplyForCourseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentGetByNameDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentGetDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentUpdateDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Enrollment;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.CourseCreationException;
import edu.sombra.coursemanagementsystem.exception.EnrollmentException;
import edu.sombra.coursemanagementsystem.exception.UserAlreadyAssignedException;
import edu.sombra.coursemanagementsystem.mapper.EnrollmentMapper;
import edu.sombra.coursemanagementsystem.repository.EnrollmentRepository;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import edu.sombra.coursemanagementsystem.service.CourseService;
import edu.sombra.coursemanagementsystem.service.EnrollmentService;
import edu.sombra.coursemanagementsystem.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Transactional
@Service
public class EnrollmentServiceImpl implements EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final UserService userService;
    private final HomeworkRepository homeworkRepository;
    private final EnrollmentMapper enrollmentMapper;

    private static final Long COURSE_LIMIT = 5L;

    @Override
    public void save(Enrollment enrollment) {
        try {
            enrollmentRepository.save(enrollment);
        } catch (DataAccessException ex) {
            log.error("Error creating course: {}", ex.getMessage(), ex);
            throw new CourseCreationException("Failed to create course", ex);
        }
    }

    @Override
    public void assignInstructor(EnrollmentDTO enrollmentDTO) {
        try {
            Course course = courseService.findByName(enrollmentDTO.getCourseName());
            User instructor = userService.findUserByEmail(enrollmentDTO.getUserEmail());
            userService.validateInstructor(instructor, RoleEnum.INSTRUCTOR);

            isUserAlreadyAssigned(course, instructor);

            Enrollment enrollment = buildEnrollment(course, instructor);
            enrollmentRepository.save(enrollment);
        } catch (DataAccessException ex) {
            log.error("Error saving instructor to the course with email: {}", enrollmentDTO.getUserEmail());
            throw new EnrollmentException("Failed to assign instructor" + ex);
        }
    }

    @Override
    public void removeUserFromCourse(Long id) {
        try {
            User user = findUserByEnrollment(id);
            if (user.getRole().equals(RoleEnum.INSTRUCTOR)) {
                if (getListOfInstructorsForCourse(id).size() > 1) {
                    removeEnrollment(id);
                } else {
                    throw new EnrollmentException("Course should have at least one instructor assigned on the course");
                }
            } else {
                removeEnrollment(id);
            }
        } catch (DataAccessException ex) {
            throw new EntityNotFoundException("Entity not found with ID: " + id);
        }
    }

    private User findUserByEnrollment(Long id) {
        try {
            return enrollmentRepository.findUserByEnrollmentId(id);
        } catch (NoResultException ex) {
            throw new EnrollmentException("Enrollment not found for id: " + id, ex);
        }
    }

    private void removeEnrollment(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found with id: " + id));

        enrollmentRepository.delete(enrollment);
    }

    private List<User> getListOfInstructorsForCourse(Long id) {
        Course course = enrollmentRepository.findCourseByEnrollmentId(id);
        return enrollmentRepository.findAssignedInstructorsForCourse(course.getId());
    }

    @Override
    public EnrollmentGetDTO findEnrolmentById(Long id) {
        return enrollmentRepository.findById(id)
                .map(enrollment -> EnrollmentGetDTO.builder()
                        .courseName(enrollment.getCourse().getName())
                        .userEmail(enrollment.getUser().getEmail())
                        .role(enrollment.getUser().getRole())
                        .build())
                .orElseThrow(() -> {
                    log.error("Error finding enrollment with id: {}", id);
                    return new EntityNotFoundException("Enrollment not found with id: " + id);
                });
    }

    @Override
    public List<EnrollmentGetByNameDTO> findEnrolmentByCourseName(String name) {
        try {
            return enrollmentRepository.findEnrollmentByCourseName(name)
                    .stream()
                    .map(this::mapTupleToEnrollmentGetByNameDTO)
                    .toList();
        } catch (DataAccessException ex) {
            log.error("Error finding enrollment with name: {}", name, ex);
            throw new EntityNotFoundException("Failed to find enrollment by name", ex);
        }
    }

    @Override
    public EnrollmentGetByNameDTO updateEnrollment(EnrollmentUpdateDTO updateDTO) {
        try {
            User user = userService.findUserById(updateDTO.getUserId());
            Course course = courseService.findById(updateDTO.getCourseId());
            Enrollment enrollment = enrollmentRepository.update(Enrollment.builder()
                    .id(updateDTO.getId())
                    .course(course)
                    .user(user)
                    .build());
            return enrollmentMapper.mapToDTO(enrollment);
        } catch (DataAccessException ex) {
            log.error("Error updating enrollment with id: {}", updateDTO.getId(), ex);
            throw new EnrollmentException("Failed to update enrollment", ex);
        }
    }

    @Override
    public void applyForCourse(EnrollmentApplyForCourseDTO applyForCourseDTO, String userEmail) {
        try {
            User user = userService.findUserByEmail(userEmail);
            if (!user.getRole().equals(RoleEnum.ADMIN)) {
                Long numberOfUserCourses = enrollmentRepository.getUserRegisteredCourseCount(user.getId());
                assignUserForLesson(applyForCourseDTO, numberOfUserCourses, user);
            } else {
                User student = userService.findUserByEmail(userEmail);
                Long numberOfUserCourses = enrollmentRepository.getUserRegisteredCourseCount(applyForCourseDTO.getUserId());
                assignUserForLesson(applyForCourseDTO, numberOfUserCourses, student);
            }
        } catch (DataAccessException ex) {
            log.error("Error applying for course with name: {}", applyForCourseDTO.getCourseName(), ex);
            throw new EnrollmentException("Failed to apply for course", ex);
        }
    }

    private void assignUserForLesson(EnrollmentApplyForCourseDTO applyForCourseDTO, Long numberOfUserCourses, User user) {
        if (numberOfUserCourses < COURSE_LIMIT) {
            Course course = courseService.findByName(applyForCourseDTO.getCourseName());
            isUserAlreadyAssigned(course, user);
            Enrollment enrollment = buildEnrollment(course, user);
            enrollmentRepository.save(enrollment);
            List<Lesson> lessons = courseService.findAllLessonsByCourse(course.getId());
            for (Lesson lesson : lessons) {
                homeworkRepository.assignUserForLesson(user.getId(), lesson.getId());
            }
        } else {
            throw new EnrollmentException("User has already assigned for 5 courses");
        }
    }

    @Override
    public void isUserAlreadyAssigned(Course course, User user) {
        boolean userAssigned = enrollmentRepository.isUserAssignedToCourse(course, user);
        if (userAssigned) {
            log.error("User is already assigned to this course");
            throw new UserAlreadyAssignedException("User is already assigned to this course");
        }
    }

    private EnrollmentGetByNameDTO mapTupleToEnrollmentGetByNameDTO(Tuple tuple) {
        String courseName = tuple.get(0, String.class);
        String firstName = tuple.get(1, String.class);
        String lastName = tuple.get(2, String.class);
        RoleEnum role = tuple.get(3, RoleEnum.class);
        String email = tuple.get(4, String.class);

        return new EnrollmentGetByNameDTO(courseName, firstName, lastName, email, role);
    }

    @Override
    public Enrollment buildEnrollment(Course course, User instructor) {
        return Enrollment.builder()
                .course(course)
                .user(instructor)
                .build();
    }

    @Override
    public List<String> findAllCoursesByUser(Long id) {
        try {
            return enrollmentRepository.findCoursesByUserId(id);
        } catch (DataAccessException ex) {
            log.error("Error finding courses for user with id: {}", id, ex);
            throw new EntityNotFoundException("Failed to find courses for user", ex);
        }
    }

    @Override
    public boolean isUserAssignedToCourse(Long userId, Long homeworkId) {
        Course course = courseService.findCourseByHomeworkId(userId, homeworkId);
        User user = userService.findUserById(userId);
        return enrollmentRepository.isUserAssignedToCourse(course, user);
    }
}
