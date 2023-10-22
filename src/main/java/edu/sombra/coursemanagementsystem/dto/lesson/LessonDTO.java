package edu.sombra.coursemanagementsystem.dto.lesson;

import edu.sombra.coursemanagementsystem.dto.homework.HomeworkWithFileDTO;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LessonDTO {
    private Long lessonId;
    private String lessonName;
    private HomeworkWithFileDTO homeworkDTO;
}
