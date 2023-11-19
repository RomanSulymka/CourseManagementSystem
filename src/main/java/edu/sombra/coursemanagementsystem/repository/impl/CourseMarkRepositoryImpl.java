package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.CourseMark;
import edu.sombra.coursemanagementsystem.repository.CourseMarkRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@AllArgsConstructor
@Repository
public class CourseMarkRepositoryImpl implements CourseMarkRepository {
    @PersistenceContext
    @Getter
    private EntityManager entityManager;
    public static final String SQL_UPSERT_USER_COURSE_MARK = "INSERT INTO user_course_marks (user_id, course_id, total_score, passed) " +
            "VALUES (?, ?, ?, ?)" +
            "ON CONFLICT (user_id, course_id) " +
            "DO UPDATE " +
            "   SET total_score = EXCLUDED.total_score, " +
            "       passed = EXCLUDED.passed; ";
    private static final String GET_ELEMENTS_BY_USER_ID_AND_COURSE_ID = "SELECT u FROM user_course_marks u WHERE u.course.id = :courseId AND u.user.id = :userId";

    @Generated
    @Override
    public Class<CourseMark> getEntityClass() {
        return CourseMark.class;
    }

    public Optional<CourseMark> findCourseMarkByUserIdAndCourseId(Long userId, Long courseId) {
        try {
            CourseMark courseMark = getEntityManager()
                    .createQuery(GET_ELEMENTS_BY_USER_ID_AND_COURSE_ID, CourseMark.class)
                    .setParameter("courseId", courseId)
                    .setParameter("userId", userId)
                    .getSingleResult();
            return Optional.of(courseMark);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void upsert(CourseMark courseMark) {
        getEntityManager().createNativeQuery(SQL_UPSERT_USER_COURSE_MARK)
                .setParameter(1, courseMark.getUser().getId())
                .setParameter(2, courseMark.getCourse().getId())
                .setParameter(3, courseMark.getTotalScore())
                .setParameter(4, courseMark.getPassed())
                .executeUpdate();
    }
}
