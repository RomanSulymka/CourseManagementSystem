package edu.sombra.coursemanagementsystem.dto.lesson;

import lombok.Data;
import lombok.Generated;

@Data
public class CreateLessonDTO {
    private String lessonName;
    private Long courseId;
}
