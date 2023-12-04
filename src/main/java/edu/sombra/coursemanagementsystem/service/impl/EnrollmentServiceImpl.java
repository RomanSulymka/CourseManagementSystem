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
    public static final String ENROLLMENT_IS_NULL = "Enrollment is null!";
    public static final String FAILED_TO_CREATE_ENROLLMENT = "Failed to create enrollment ";
    public static final String FAILED_TO_ASSIGN_INSTRUCTOR = "Failed to assign instructor";
    public static final String ERROR_SAVING_INSTRUCTOR_TO_THE_COURSE_WITH_EMAIL = "Error saving instructor to the course with email: {}";
    public static final String COURSE_SHOULD_HAVE_AT_LEAST_ONE_INSTRUCTOR_ASSIGNED_ON_THE_COURSE = "Course should have at least one instructor assigned on the course";
    public static final String ENTITY_NOT_FOUND_WITH_ID = "Entity not found with ID: ";
    public static final String ERROR_FINDING_ENROLLMENT_WITH_ID = "Error finding enrollment with id: {}";
    public static final String ENROLLMENT_NOT_FOUND_WITH_ID = "Enrollment not found with id: ";
    public static final String ERROR_FINDING_ENROLLMENT_WITH_NAME = "Error finding enrollment with name: {}";
    public static final String FAILED_TO_FIND_ENROLLMENT_BY_NAME = "Failed to find enrollment by name";
    public static final String ELEMENTS_ARE_EMPTY = "Elements are empty!";
    public static final String USER_HAS_ALREADY_ASSIGNED_FOR_5_COURSES = "User has already assigned for 5 courses";
    public static final String USER_IS_ALREADY_ASSIGNED_TO_THIS_COURSE = "User is already assigned to this course";
    public static final String ERROR_FINDING_COURSES_FOR_USER_WITH_ID = "Error finding courses for user with id: {}";
    public static final String FAILED_TO_FIND_COURSES_FOR_USER = "Failed to find courses for user";

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
                throw new EnrollmentException(ENROLLMENT_IS_NULL);
            }
            enrollmentRepository.save(enrollment);
        } catch (EnrollmentException ex) {
            log.error(FAILED_TO_CREATE_ENROLLMENT + ex.getMessage(), ex);
            throw new EnrollmentException(FAILED_TO_CREATE_ENROLLMENT, ex);
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
            log.error(ERROR_SAVING_INSTRUCTOR_TO_THE_COURSE_WITH_EMAIL, enrollmentDTO.getUserEmail());
            throw new EnrollmentException(FAILED_TO_ASSIGN_INSTRUCTOR);
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
                    throw new EnrollmentException(COURSE_SHOULD_HAVE_AT_LEAST_ONE_INSTRUCTOR_ASSIGNED_ON_THE_COURSE);
                }
            } else {
                removeEnrollment(id);
            }
        } catch (NullPointerException ex) {
            throw new EntityNotFoundException(ENTITY_NOT_FOUND_WITH_ID + id);
        }
    }

    public User findUserByEnrollment(Long id) {
        try {
            return enrollmentRepository.findUserByEnrollmentId(id);
        } catch (EntityNotFoundException ex) {
            throw new EnrollmentException(ENTITY_NOT_FOUND_WITH_ID + id, ex);
        }
    }

    private void removeEnrollment(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_WITH_ID + id));

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
                    log.error(ERROR_FINDING_ENROLLMENT_WITH_ID, id);
                    return new EntityNotFoundException(ENROLLMENT_NOT_FOUND_WITH_ID + id);
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
            log.error(ERROR_FINDING_ENROLLMENT_WITH_NAME, name, ex);
            throw new EntityNotFoundException(FAILED_TO_FIND_ENROLLMENT_BY_NAME, ex);
        }
    }

    @Override
    public EnrollmentGetByNameDTO updateEnrollment(EnrollmentUpdateDTO updateDTO) {
        if (updateDTO.getCourseId() == null && updateDTO.getUserId() == null & updateDTO.getId() == null) {
            throw new EnrollmentException(ELEMENTS_ARE_EMPTY);
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
            throw new EnrollmentException(USER_HAS_ALREADY_ASSIGNED_FOR_5_COURSES);
        }
    }

    @Override
    public void isUserAlreadyAssigned(Course course, User user) {
        boolean userAssigned = enrollmentRepository.isUserAssignedToCourse(course, user);
        if (userAssigned) {
            log.error(USER_IS_ALREADY_ASSIGNED_TO_THIS_COURSE);
            throw new UserAlreadyAssignedException(USER_IS_ALREADY_ASSIGNED_TO_THIS_COURSE);
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
            log.error(ERROR_FINDING_COURSES_FOR_USER_WITH_ID, id, ex);
            throw new EntityNotFoundException(FAILED_TO_FIND_COURSES_FOR_USER, ex);
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
