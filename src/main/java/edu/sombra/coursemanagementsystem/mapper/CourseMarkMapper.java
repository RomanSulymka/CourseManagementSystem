package edu.sombra.coursemanagementsystem.mapper;

import edu.sombra.coursemanagementsystem.dto.course.CourseMarkResponseDTO;
import edu.sombra.coursemanagementsystem.entity.CourseMark;
import org.springframework.stereotype.Component;

@Component
public class CourseMarkMapper {

    public CourseMarkResponseDTO toDTO(CourseMark courseMark) {
        return CourseMarkResponseDTO
                .builder()
                .id(courseMark.getId())
                .passed(courseMark.getPassed())
                .userId(courseMark.getUser().getId())
                .userEmail(courseMark.getUser().getEmail())
                .totalScore(courseMark.getTotalScore())
                .course(courseMark.getCourse())
                .build();
    }
}
