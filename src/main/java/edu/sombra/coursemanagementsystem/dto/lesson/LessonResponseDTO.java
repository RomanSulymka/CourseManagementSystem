package edu.sombra.coursemanagementsystem.dto.lesson;

import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonResponseDTO {
    private Long id;
    private String name;
    private CourseResponseDTO course;
}
