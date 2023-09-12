package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.EnrollmentDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Enrollment;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.InstructorsAlreadyAssignedException;
import edu.sombra.coursemanagementsystem.repository.EnrollmentRepository;
import edu.sombra.coursemanagementsystem.service.CourseService;
import edu.sombra.coursemanagementsystem.service.EnrollmentService;
import edu.sombra.coursemanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Slf4j
@Service
public class EnrollmentServiceImpl implements EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final UserService userService;

    @Override
    public void assignInstructor(EnrollmentDTO enrollmentDTO) {
        Course course = courseService.findByName(enrollmentDTO.getCourseName());

        List<User> instructors = userService.findUsersByEmails(enrollmentDTO.getInstructorsEmail());

        userService.validateInstructors(instructors, RoleEnum.INSTRUCTOR);

        checkIfInstructorsAlreadyAssigned(course, instructors);

        List<Enrollment> enrollments = createEnrollmentsForInstructors(course, instructors);
        enrollmentRepository.saveAll(enrollments);
    }

    private List<Enrollment> createEnrollmentsForInstructors(Course course, List<User> instructors) {
        return instructors.stream()
                .map(instructor -> Enrollment.builder()
                        .course(course)
                        .user(instructor)
                        .build())
                .toList();
    }

    private void checkIfInstructorsAlreadyAssigned(Course course, List<User> instructors) {
        boolean instructorsAssigned = enrollmentRepository.areUsersAssignedToCourse(course, instructors);

        if (instructorsAssigned) {
            throw new InstructorsAlreadyAssignedException("Instructors are already assigned to this course");
        }
    }

}
