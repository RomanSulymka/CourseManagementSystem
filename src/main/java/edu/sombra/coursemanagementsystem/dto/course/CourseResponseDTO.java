package edu.sombra.coursemanagementsystem.dto.course;

import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDTO {
    private Long courseId;
    private String courseName;
    private CourseStatus status;
    private LocalDate startDate;
    private Boolean started;
}
