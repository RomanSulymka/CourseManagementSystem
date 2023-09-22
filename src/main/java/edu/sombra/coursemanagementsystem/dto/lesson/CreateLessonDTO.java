package edu.sombra.coursemanagementsystem.dto.lesson;

import lombok.Data;

@Data
public class CreateLessonDTO {
    private String lessonName;
    private Long courseId;
}
