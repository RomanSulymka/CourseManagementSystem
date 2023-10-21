package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Slf4j
@AllArgsConstructor
@Repository
public class HomeworkRepositoryImpl implements HomeworkRepository {
    @PersistenceContext
    private EntityManager entityManager;
    private final String GET_AVERAGE_MARK_BY_USER = "SELECT AVG(h.mark) FROM homework h INNER JOIN lessons l on l.id = h.lesson.id INNER JOIN courses c on c.id = l.course.id WHERE h.user.id = :userId AND l.course.id = :courseId";

    @Override
    public void setMark(Long homeworkId, Long mark) {
        getEntityManager()
                .createNativeQuery("UPDATE homework SET mark =:mark WHERE id =:id")
                .setParameter("id", homeworkId)
                .setParameter("mark", mark)
                .executeUpdate();
    }

    @Override
    public Double calculateAverageHomeworksMarkByUserId(Long userId, Long courseId) {
        return getEntityManager().createQuery(GET_AVERAGE_MARK_BY_USER, Double.class)
                .setParameter("userId", userId)
                .setParameter("courseId", courseId)
                .getSingleResult();
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Class<Homework> getEntityClass() {
        return Homework.class;
    }
}
