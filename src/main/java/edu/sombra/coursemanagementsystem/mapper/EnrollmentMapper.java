package edu.sombra.coursemanagementsystem.mapper;

import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentGetByNameDTO;
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

}
