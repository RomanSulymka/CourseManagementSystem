package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.repository.LessonRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Generated;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LessonRepositoryImpl implements LessonRepository {
    public static final String GET_LESSONS_BY_USER = "SELECT l FROM lessons l INNER JOIN enrollments e on l.course.id = e.course.id" +
            " INNER JOIN users u on e.user.id = u.id " +
            " WHERE u.id =: userId";
    @PersistenceContext
    private EntityManager entityManager;

    private static final String GET_LESSONS_BY_HOMEWORK_ID = "SELECT l FROM homework h INNER JOIN lessons l on l.id = h.lesson.id WHERE h.id = :homeworkId";

    private static final String GET_ALL_LESSONS_BY_COURSE_ID = "SELECT l FROM lessons l WHERE l.course.id =: courseId";


    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Generated
    @Override
    public Class<Lesson> getEntityClass() {
        return Lesson.class;
    }

    @Override
    public List<Lesson> findAllByCourseId(Long courseId) {
        return getEntityManager().createQuery(GET_ALL_LESSONS_BY_COURSE_ID, Lesson.class)
                .setParameter("courseId", courseId)
                .getResultList();
    }

    @Override
    public Optional<Lesson> findLessonByHomeworkId(Long homeworkId) {
        return Optional.ofNullable(entityManager.createQuery(GET_LESSONS_BY_HOMEWORK_ID, Lesson.class)
                .setParameter("homeworkId", homeworkId)
                .getSingleResult());
    }

    @Override
    public List<Lesson> findAllLessonsByUserId(Long userId) {
        return getEntityManager().createQuery(GET_LESSONS_BY_USER, Lesson.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
