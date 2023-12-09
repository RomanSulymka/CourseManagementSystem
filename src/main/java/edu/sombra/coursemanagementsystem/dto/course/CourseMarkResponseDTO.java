package edu.sombra.coursemanagementsystem.dto.course;

import edu.sombra.coursemanagementsystem.entity.Course;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CourseMarkResponseDTO {
    private Long id;
    private BigDecimal totalScore;
    private Long userId;
    private String userEmail;
    private Course course;
    private Boolean passed;
}
