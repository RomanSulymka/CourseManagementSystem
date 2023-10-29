package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.CourseFeedback;
import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;

import java.util.Optional;

public interface CourseFeedbackRepository extends BaseRepository<CourseFeedback, Long> {
    Optional<CourseFeedback> findFeedback(Long studentId, Long courseId);
}
