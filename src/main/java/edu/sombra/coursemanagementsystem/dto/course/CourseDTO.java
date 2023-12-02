package edu.sombra.coursemanagementsystem.dto.course;

import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;
import lombok.NonNull;

import java.time.LocalDate;

@Generated
@Data
@Builder
public class CourseDTO {
    @NonNull
    private String name;

    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    @Temporal(TemporalType.DATE)
    private LocalDate startDate;

    private Boolean started;

    @NonNull
    private String instructorEmail;

    private Long numberOfLessons;
}
