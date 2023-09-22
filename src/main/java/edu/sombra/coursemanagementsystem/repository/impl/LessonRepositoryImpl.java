package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.repository.LessonRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LessonRepositoryImpl implements LessonRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public EntityManager entityManager() {
        return entityManager;
    }

    @Override
    public Class<Lesson> getEntityClass() {
        return Lesson.class;
    }

    @Override
    public List<Lesson> findAllByCourseId(Long courseId) {
        return entityManager().createQuery("SELECT l FROM lessons l WHERE l.course.id =: courseId", Lesson.class)
                .setParameter("courseId", courseId)
                .getResultList();
    }
}
