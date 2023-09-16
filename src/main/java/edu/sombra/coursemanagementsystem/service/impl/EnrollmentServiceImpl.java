package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.EnrollmentApplyForCourseDTO;
import edu.sombra.coursemanagementsystem.dto.EnrollmentDTO;
import edu.sombra.coursemanagementsystem.dto.EnrollmentGetByNameDTO;
import edu.sombra.coursemanagementsystem.dto.EnrollmentGetDTO;
import edu.sombra.coursemanagementsystem.dto.EnrollmentUpdateDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Enrollment;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.EnrollmentException;
import edu.sombra.coursemanagementsystem.exception.UserAlreadyAssignedException;
import edu.sombra.coursemanagementsystem.repository.EnrollmentRepository;
import edu.sombra.coursemanagementsystem.service.CourseService;
import edu.sombra.coursemanagementsystem.service.EnrollmentService;
import edu.sombra.coursemanagementsystem.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Slf4j
@Transactional
@Service
public class EnrollmentServiceImpl implements EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final UserService userService;

    private static final Long COURSE_LIMIT = 5L;

    @Override
    public void assignInstructor(EnrollmentDTO enrollmentDTO) {
        try {
            Course course = courseService.findByName(enrollmentDTO.getCourseName());
            User instructor = userService.findUserByEmail(enrollmentDTO.getInstructorEmail());
            userService.validateInstructor(instructor, RoleEnum.INSTRUCTOR);

            isUserAlreadyAssigned(course, instructor);

            Enrollment enrollment = buildEnrollment(course, instructor);
            enrollmentRepository.save(enrollment);
        } catch (DataAccessException ex) {
            log.error("Error saving instructor to the course with email: {}", enrollmentDTO.getInstructorEmail());
            throw new EnrollmentException("Failed to assign instructor" + ex);
        }
    }

    @Override
    public void removeUserFromCourse(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found with id: " + id));

        enrollmentRepository.delete(enrollment);
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
            User user = userService.findUserByEmail(updateDTO.getUserEmail());
            Course course = courseService.findByName(updateDTO.getCourseName());
            Enrollment enrollment = enrollmentRepository.update(Enrollment.builder()
                    .id(updateDTO.getId())
                    .course(course)
                    .user(user)
                    .build());

            return EnrollmentGetByNameDTO.builder()
                    .name(enrollment.getCourse().getName())
                    .firstName(enrollment.getUser().getFirstName())
                    .lastName(enrollment.getUser().getLastName())
                    .email(enrollment.getUser().getEmail())
                    .role(enrollment.getUser().getRole())
                    .build();
        } catch (DataAccessException ex) {
            log.error("Error updating enrollment with id: {}", updateDTO.getId(), ex);
            throw new EnrollmentException("Failed to update enrollment", ex);
        }
    }

    @Override
    public void applyForCourse(EnrollmentApplyForCourseDTO applyForCourseDTO) {
        try {
            Long numberOfUserCourses = enrollmentRepository.getUserRegisteredCourseCount(applyForCourseDTO.getUserId());
            if (numberOfUserCourses < COURSE_LIMIT) {
                User user = userService.findUserById(applyForCourseDTO.getUserId());
                Course course = courseService.findByName(applyForCourseDTO.getCourseName());
                isUserAlreadyAssigned(course, user);
                Enrollment enrollment = buildEnrollment(course, user);
                enrollmentRepository.save(enrollment);
            } else {
                throw new EnrollmentException("User has already assigned for 5 courses");
            }
        } catch (DataAccessException ex) {
            log.error("Error applying for course with name: {}", applyForCourseDTO.getCourseName(), ex);
            throw new EnrollmentException("Failed to apply for course", ex);
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

    private Enrollment buildEnrollment(Course course, User instructor) {
        return Enrollment.builder()
                .course(course)
                .user(instructor)
                .build();
    }

    private void isUserAlreadyAssigned(Course course, User user) {
        boolean userAssigned = enrollmentRepository.isUserAssignedToCourse(course, user);
        if (userAssigned) {
            log.error("User is already assigned to this course");
            throw new UserAlreadyAssignedException("User is already assigned to this course");
        }
    }

}
