package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.Generated;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Repository
public class HomeworkRepositoryImpl implements HomeworkRepository {
    @PersistenceContext
    private EntityManager entityManager;
    public static final String GET_ALL_HOMEWORKS_BY_USER = "SELECT h FROM homework h where user.id =: userId";

    private static final String GET_AVERAGE_MARK_BY_USER = "SELECT AVG(h.mark) FROM homework h INNER JOIN lessons l on l.id = h.lesson.id " +
            "INNER JOIN courses c on c.id = l.course.id WHERE h.user.id = :userId AND l.course.id = :courseId";

    private static final String GET_HOMEWORK_BY_USER_ID_AND_LESSON_ID = "SELECT h from homework h WHERE h.user.id =: userId " +
            "AND h.lesson.id =: lessonId";

    private static final String INSERT_USER_FOR_LESSON = "INSERT INTO homework (user_id, lesson_id) VALUES (:userId, :lessonId )";

    private static final String GET_HOMEWORKS_BY_COURSE_ID = "SELECT h FROM homework h INNER JOIN lessons l on l.id = h.lesson.id " +
            "WHERE l.course.id =: courseId";
    public static final String GET_HOMEWORKS_WITH_INSTRUCTOR_ACCESS = "SELECT h " +
            " FROM homework h " +
            "         JOIN users u_student ON h.user.id = u_student.id " +
            "         JOIN enrollments e_student ON u_student.id = e_student.user.id " +
            "         JOIN courses c ON e_student.course.id = c.id " +
            "         JOIN enrollments e_instructor ON c.id = e_instructor.course.id " +
            "         JOIN users u_instructor ON e_instructor.user.id = u_instructor.id " +
            " WHERE u_student.role = 'STUDENT' " +
            "  AND u_instructor.role = 'INSTRUCTOR' " +
            "  AND u_instructor.id = :instructor_id";
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
        return getEntityManager().createQuery(GET_HOMEWORK_BY_USER_ID_AND_LESSON_ID, Homework.class)
                .setParameter("userId", userId)
                .setParameter("lessonId", lessonId)
                .getResultStream()
                .findFirst();
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
    public List<Homework> findAllHomeworksWithInstructorAccess(Long userId) {
        return getEntityManager().createQuery(GET_HOMEWORKS_WITH_INSTRUCTOR_ACCESS, Homework.class)
                .setParameter("instructor_id", userId)
                .getResultList();
    }

    @Generated
    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Generated
    @Override
    public Class<Homework> getEntityClass() {
        return Homework.class;
    }
}
