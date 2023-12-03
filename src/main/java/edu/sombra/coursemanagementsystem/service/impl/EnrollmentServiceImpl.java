package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
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
import edu.sombra.coursemanagementsystem.exception.EnrollmentException;
import edu.sombra.coursemanagementsystem.exception.UserAlreadyAssignedException;
import edu.sombra.coursemanagementsystem.mapper.CourseMapper;
import edu.sombra.coursemanagementsystem.mapper.EnrollmentMapper;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.repository.EnrollmentRepository;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.CourseService;
import edu.sombra.coursemanagementsystem.service.EnrollmentService;
import edu.sombra.coursemanagementsystem.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final CourseMapper courseMapper;
    private final CourseRepository courseRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final HomeworkRepository homeworkRepository;
    private final EnrollmentMapper enrollmentMapper;

    private static final Long COURSE_LIMIT = 5L;

    @Override
    public void save(Enrollment enrollment) {
        try {
            if (enrollment == null) {
                throw new EnrollmentException("Enrollment is null!");
            }
            enrollmentRepository.save(enrollment);
        } catch (EnrollmentException ex) {
            log.error("Error creating enrollment: {}", ex.getMessage(), ex);
            throw new EnrollmentException("Failed to create enrollment", ex);
        }
    }

    @Override
    public void assignInstructor(EnrollmentDTO enrollmentDTO) {
        try {
            Course course = courseRepository.findByName(enrollmentDTO.getCourseName())
                    .orElseThrow();
            User instructor = userRepository.findUserByEmail(enrollmentDTO.getUserEmail());
            userService.validateInstructor(instructor, RoleEnum.INSTRUCTOR);

            isUserAlreadyAssigned(course, instructor);

            Enrollment enrollment = buildEnrollment(course, instructor);
            enrollmentRepository.save(enrollment);
        } catch (EnrollmentException ex) {
            log.error("Error saving instructor to the course with email: {}", enrollmentDTO.getUserEmail());
            throw new EnrollmentException("Failed to assign instructor");
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
        } catch (NullPointerException ex) {
            throw new EntityNotFoundException("Entity not found with ID: " + id);
        }
    }

    public User findUserByEnrollment(Long id) {
        try {
            return enrollmentRepository.findUserByEnrollmentId(id);
        } catch (EntityNotFoundException ex) {
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
        } catch (RuntimeException ex) {
            log.error("Error finding enrollment with name: {}", name, ex);
            throw new EntityNotFoundException("Failed to find enrollment by name", ex);
        }
    }

    @Override
    public EnrollmentGetByNameDTO updateEnrollment(EnrollmentUpdateDTO updateDTO) {
        if (updateDTO.getCourseId() == null && updateDTO.getUserId() == null & updateDTO.getId() == null) {
            throw new EnrollmentException("Elements are empty!");
        }
        User user = userRepository.findById(updateDTO.getUserId()).orElseThrow();
        Course course = courseRepository.findById(updateDTO.getCourseId()).orElseThrow();
        Enrollment enrollment = enrollmentRepository.update(Enrollment.builder()
                .id(updateDTO.getId())
                .course(course)
                .user(user)
                .build());
        return enrollmentMapper.mapToDTO(enrollment);
    }

    @Override
    public void applyForCourse(EnrollmentApplyForCourseDTO applyForCourseDTO, String userEmail) {
        User user = userRepository.findUserByEmail(userEmail);
        if (!user.getRole().equals(RoleEnum.ADMIN)) {
            Long numberOfUserCourses = enrollmentRepository.getUserRegisteredCourseCount(user.getId());
            assignUserForLesson(applyForCourseDTO, numberOfUserCourses, user);
        } else {
            User student = userRepository.findUserByEmail(userEmail);
            Long numberOfUserCourses = enrollmentRepository.getUserRegisteredCourseCount(applyForCourseDTO.getUserId());
            assignUserForLesson(applyForCourseDTO, numberOfUserCourses, student);
        }
    }

    private void assignUserForLesson(EnrollmentApplyForCourseDTO applyForCourseDTO, Long numberOfUserCourses, User user) {
        if (numberOfUserCourses < COURSE_LIMIT) {
            Course course = courseRepository.findByName(applyForCourseDTO.getCourseName()).orElseThrow();
            isUserAlreadyAssigned(course, user);
            Enrollment enrollment = buildEnrollment(course, user);
            enrollmentRepository.save(enrollment);
            List<Lesson> lessons = courseRepository.findAllLessonsInCourse(course.getId()).orElseThrow(EntityNotFoundException::new);
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
        } catch (EntityNotFoundException ex) {
            log.error("Error finding courses for user with id: {}", id, ex);
            throw new EntityNotFoundException("Failed to find courses for user", ex);
        }
    }

    @Override
    public boolean isUserAssignedToCourse(Long userId, Long homeworkId) {
        CourseResponseDTO courseResponseDTO = courseService.findCourseByHomeworkId(userId, homeworkId);
        Course course = courseMapper.fromResponseDTO(courseResponseDTO);
        User user = userRepository.findById(userId).orElseThrow();
        return enrollmentRepository.isUserAssignedToCourse(course, user);
    }
}
