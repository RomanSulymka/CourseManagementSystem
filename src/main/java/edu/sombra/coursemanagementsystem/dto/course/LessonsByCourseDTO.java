package edu.sombra.coursemanagementsystem.dto.course;


import edu.sombra.coursemanagementsystem.dto.lesson.LessonDTO;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
public class LessonsByCourseDTO {
    private Long courseId;
    private String courseName;
    private String feedback;
    private List<LessonDTO> lessonDTO;
    private BigDecimal totalScore;
    private boolean passed;
}
