package edu.sombra.coursemanagementsystem.mapper;

import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentGetByNameDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentGetDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentResponseDTO;
import edu.sombra.coursemanagementsystem.entity.Enrollment;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {
    public EnrollmentGetByNameDTO mapToDTO(Enrollment enrollment) {
        return EnrollmentGetByNameDTO.builder()
                .name(enrollment.getCourse().getName())
                .firstName(enrollment.getUser().getFirstName())
                .lastName(enrollment.getUser().getLastName())
                .email(enrollment.getUser().getEmail())
                .role(enrollment.getUser().getRole())
                .build();
    }

    public EnrollmentGetDTO mapToEnrollmentGetDTO(Enrollment enrollment) {
        return EnrollmentGetDTO.builder()
                .courseName(enrollment.getCourse().getName())
                .userEmail(enrollment.getUser().getEmail())
                .role(enrollment.getUser().getRole())
                .build();
    }

    public EnrollmentResponseDTO mapToResponseDTO(Enrollment enrollment) {
        return EnrollmentResponseDTO.builder()
                .enrollmentId(enrollment.getId())
                .courseId(enrollment.getCourse().getId())
                .courseName(enrollment.getCourse().getName())
                .userId(enrollment.getUser().getId())
                .userEmail(enrollment.getUser().getEmail())
                .role(enrollment.getUser().getRole())
                .build();
    }
}
