package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentApplyForCourseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentGetByNameDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentGetDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentUpdateDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Enrollment;
import edu.sombra.coursemanagementsystem.entity.User;

import java.util.List;

public interface EnrollmentService {
    void save(Enrollment enrollment);

    void assignInstructor(EnrollmentDTO enrollmentDTO);

    void removeUserFromCourse(Long id);

    EnrollmentGetDTO findEnrolmentById(Long id);

    List<EnrollmentGetByNameDTO> findEnrolmentByCourseName(String name);

    EnrollmentGetByNameDTO updateEnrollment(EnrollmentUpdateDTO updateDTO);

    void applyForCourse(EnrollmentApplyForCourseDTO applyForCourseDTO, String userEmail);

    void isUserAlreadyAssigned(Course course, User user);

    Enrollment buildEnrollment(Course course, User user);

    List<CourseResponseDTO> findAllCoursesByUser(Long id);

    boolean isUserAssignedToCourse(Long userId, Long homeworkId);
}
