package edu.sombra.coursemanagementsystem.dto.lesson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateLessonDTO {
    private Long id;
    private Long courseId;
    private String name;
}
