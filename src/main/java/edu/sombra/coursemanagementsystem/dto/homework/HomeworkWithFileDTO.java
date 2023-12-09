package edu.sombra.coursemanagementsystem.dto.homework;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Generated
@Builder
@Data
public class HomeworkWithFileDTO {
    private Long homeworkId;
    private Long mark;
    private String fileName;
}
