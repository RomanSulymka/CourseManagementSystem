package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.CourseFeedback;
import edu.sombra.coursemanagementsystem.repository.CourseFeedbackRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.Generated;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CourseFeedbackRepositoryImpl implements CourseFeedbackRepository {
    @Getter
    @PersistenceContext
    private EntityManager entityManager;

    private static final String GET_FEEDBACK_BY_USER_AND_COURSE = "SELECT c FROM course_feedback c " +
            "WHERE c.course.id =: courseId AND c.student.id =: userId";

    @Override
    public Optional<CourseFeedback> findFeedback(Long studentId, Long courseId) {

        TypedQuery<CourseFeedback> typedQuery = getEntityManager()
                .createQuery(GET_FEEDBACK_BY_USER_AND_COURSE, CourseFeedback.class)
                .setParameter("courseId", courseId)
                .setParameter("userId", studentId);

        return typedQuery.getResultList().stream()
                .findFirst();
    }

    @Generated
    @Override
    public Class<CourseFeedback> getEntityClass() {
        return CourseFeedback.class;
    }
}
