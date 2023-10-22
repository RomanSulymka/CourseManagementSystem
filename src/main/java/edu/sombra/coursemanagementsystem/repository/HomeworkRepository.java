package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;

import java.util.Optional;

public interface HomeworkRepository extends BaseRepository<Homework, Long> {
    void setMark(Long homeworkId, Long mark);

    Double calculateAverageHomeworksMarkByUserId(Long userId, Long id);

    Optional<Homework> findByUserAndLessonId(Long userId, Long lessonId);
}
