package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.EnrollmentApplyForCourseDTO;
import edu.sombra.coursemanagementsystem.dto.EnrollmentDTO;
import edu.sombra.coursemanagementsystem.dto.EnrollmentGetByNameDTO;
import edu.sombra.coursemanagementsystem.dto.EnrollmentGetDTO;
import edu.sombra.coursemanagementsystem.dto.EnrollmentUpdateDTO;

import java.util.List;

public interface EnrollmentService {

    void assignInstructor(EnrollmentDTO enrollmentDTO);

    void removeUserFromCourse(Long id);

    EnrollmentGetDTO findEnrolmentById(Long id);

    List<EnrollmentGetByNameDTO> findEnrolmentByCourseName(String name);

    EnrollmentGetByNameDTO updateEnrollment(EnrollmentUpdateDTO updateDTO);

    void applyForCourse(EnrollmentApplyForCourseDTO applyForCourseDTO);
}
