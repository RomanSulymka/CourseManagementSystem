package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.CourseMark;
import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;

import java.util.Optional;

public interface CourseMarkRepository extends BaseRepository<CourseMark, Long> {
    Optional<CourseMark> findCourseMarkByUserIdAndCourseId(Long userId, Long courseId);
    void upsert(CourseMark courseMark);
}
