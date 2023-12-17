package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface HomeworkRepository extends BaseRepository<Homework, Long> {
    void setMark(Long homeworkId, Long mark);

    Double calculateAverageHomeworksMarkByUserId(Long userId, Long courseId);

    Optional<Homework> findByUserAndLessonId(Long userId, Long lessonId);

    void assignUserForLesson(Long userId, Long lessonId);

    List<Homework> findHomeworksByCourse(Long courseId);

    boolean isUserUploadedHomework(Long fileId, Long studentId);

    List<Homework> findAllByUser(Long userId);

    List<Homework> findAllHomeworksWithInstructorAccess(Long userId);
}
