package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.lesson.CreateLessonDTO;
import edu.sombra.coursemanagementsystem.entity.Lesson;

import java.util.List;

public interface LessonService {
    Lesson save(CreateLessonDTO lessonDTO);

    List<Lesson> findAllLessons();

    List<Lesson> findAllLessonsByCourse(Long courseId);
}
