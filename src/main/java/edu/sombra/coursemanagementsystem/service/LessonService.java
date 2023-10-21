package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.lesson.CreateLessonDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Lesson;

import java.util.List;

public interface LessonService {
    Lesson save(CreateLessonDTO lessonDTO);

    Lesson findById(Long id);

    List<Lesson> findAllLessons();

    List<Lesson> findAllLessonsByCourse(Long courseId);

    List<Lesson> generateAndAssignLessons(Long numberOfLessons, Course course);

    void deleteLesson(Long id);

    Lesson editLesson(Lesson lesson);

    Lesson findLessonByHomeworkId(Long homeworkId);
}
