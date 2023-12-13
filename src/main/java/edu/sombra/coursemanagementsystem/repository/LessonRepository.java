package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends BaseRepository<Lesson, Long> {
    List<Lesson> findAllByCourseId(Long courseId);
    Optional<Lesson> findLessonByHomeworkId(Long homeworkId);

    List<Lesson> findAllLessonsByUserId(Long id);
}
