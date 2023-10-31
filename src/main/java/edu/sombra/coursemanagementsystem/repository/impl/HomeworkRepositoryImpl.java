package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Repository
public class HomeworkRepositoryImpl implements HomeworkRepository {
    public static final String GET_ALL_HOMEWORKS_BY_USER = "SELECT h FROM homework h where user.id =: userId";
    @PersistenceContext
    private EntityManager entityManager;

    private static final String GET_AVERAGE_MARK_BY_USER = "SELECT AVG(h.mark) FROM homework h INNER JOIN lessons l on l.id = h.lesson.id " +
            "INNER JOIN courses c on c.id = l.course.id WHERE h.user.id = :userId AND l.course.id = :courseId";

    private static final String GET_HOMEWORK_BY_USER_ID_AND_LESSON_ID = "SELECT h from homework h WHERE h.user.id =: userId " +
            "AND h.lesson.id =: lessonId";

    private static final String INSERT_USER_FOR_LESSON = "INSERT INTO homework (user_id, lesson_id) VALUES (:userId, :lessonId )";

    private static final String GET_HOMEWORKS_BY_COURSE_ID = "SELECT h FROM homework h INNER JOIN lessons l on l.id = h.lesson.id " +
            "WHERE l.course.id =: courseId";

    private static final String GET_HOMEWORK_BY_FILE_AND_USER_ID = "SELECT h FROM homework h " +
            "WHERE h.user.id = :studentId AND h.file.id = :fileId";

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
    public Optional<Homework> findByUserAndLessonId(Long userId, Long lessonId) {
        return Optional.ofNullable(getEntityManager().createQuery(GET_HOMEWORK_BY_USER_ID_AND_LESSON_ID, Homework.class)
                .setParameter("userId", userId)
                .setParameter("lessonId", lessonId)
                .getSingleResult());
    }

    @Override
    public void assignUserForLesson(Long userId, Long lessonId) {
        getEntityManager().createNativeQuery(INSERT_USER_FOR_LESSON)
                .setParameter("userId", userId)
                .setParameter("lessonId", lessonId)
                .executeUpdate();
    }

    @Override
    public List<Homework> findHomeworksByCourse(Long courseId) {
        return getEntityManager().createQuery(GET_HOMEWORKS_BY_COURSE_ID, Homework.class)
                .setParameter("courseId", courseId)
                .getResultList();
    }

    @Override
    public boolean isUserUploadedHomework(Long fileId, Long studentId) {
        try {
            Homework homework = getEntityManager()
                    .createQuery(GET_HOMEWORK_BY_FILE_AND_USER_ID, Homework.class)
                    .setParameter("studentId", studentId)
                    .setParameter("fileId", fileId)
                    .getSingleResult();
            return homework != null;
        } catch (NoResultException e) {
            return false;
        }
    }

    @Override
    public List<Homework> findAllByUser(Long userId) {
        return getEntityManager().createQuery(GET_ALL_HOMEWORKS_BY_USER, Homework.class)
                .setParameter("userId", userId)
                .getResultList();
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
