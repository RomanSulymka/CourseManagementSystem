package edu.sombra.coursemanagementsystem.dto.course;

import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;

@Data
@Builder
public class CourseDTO {
    @NonNull
    private String name;

    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    @Temporal(TemporalType.DATE)
    private LocalDate startDate;

    @NonNull
    private String instructorEmail;

    private Long numberOfLessons;
}
