package edu.sombra.coursemanagementsystem.dto.homework;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetHomeworkByLessonDTO {
    private Long userId;
    private Long lessonId;
}
