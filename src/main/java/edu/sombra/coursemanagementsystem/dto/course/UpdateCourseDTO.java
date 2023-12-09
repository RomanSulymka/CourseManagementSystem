package edu.sombra.coursemanagementsystem.dto.course;

import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseDTO {
    private Long id;

    private String name;

    private CourseStatus status;

    private LocalDate startDate;

    private Boolean started;
}
