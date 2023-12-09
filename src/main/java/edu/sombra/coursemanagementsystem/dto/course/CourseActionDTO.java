package edu.sombra.coursemanagementsystem.dto.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseActionDTO {
    private Long courseId;
    private String action;
}
