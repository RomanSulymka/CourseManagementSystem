package edu.sombra.coursemanagementsystem.dto.homework;

import edu.sombra.coursemanagementsystem.entity.Lesson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetHomeworkDTO {
    private Long id;
    private Long mark;
    private Long userId;
    private String userEmail;
    private Lesson lesson;
    private String fileName;
}
