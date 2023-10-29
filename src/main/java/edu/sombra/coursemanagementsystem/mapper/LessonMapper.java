package edu.sombra.coursemanagementsystem.mapper;

import edu.sombra.coursemanagementsystem.dto.lesson.LessonDTO;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class LessonMapper {
    private final HomeworkRepository homeworkRepository;
    private final HomeworkMapper homeworkMapper;

    public List<LessonDTO> toDTO(List<Lesson> lessons, Long studentId) {
        return lessons.stream()
                .map(lesson -> toDTO(lesson, studentId))
                .toList();
    }

    public LessonDTO toDTO(Lesson lesson, Long studentId) {
        Homework homework = homeworkRepository.findByUserAndLessonId(studentId, lesson.getId())
                .orElseThrow(EntityNotFoundException::new);
        return LessonDTO.builder()
                .lessonId(lesson.getId())
                .lessonName(lesson.getName())
                .homeworkDTO(homeworkMapper.toDTO(homework))
                .build();
    }
}
