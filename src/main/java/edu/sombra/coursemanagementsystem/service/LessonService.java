package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.lesson.CreateLessonDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.LessonResponseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.UpdateLessonDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Lesson;

import java.util.List;

public interface LessonService {
    LessonResponseDTO save(CreateLessonDTO lessonDTO);

    LessonResponseDTO findById(Long id, String userEmail);

    List<LessonResponseDTO> findAllLessons();

    List<LessonResponseDTO> findAllLessonsByCourse(Long courseId);

    List<Lesson> generateAndAssignLessons(Long numberOfLessons, Course course);

    void deleteLesson(Long id);

    LessonResponseDTO editLesson(UpdateLessonDTO lesson);

    Lesson findLessonByHomeworkId(Long homeworkId);
}
