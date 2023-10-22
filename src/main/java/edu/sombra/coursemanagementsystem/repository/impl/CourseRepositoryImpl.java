package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.query.SqlQueryConstants;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class CourseRepositoryImpl implements CourseRepository {
    @PersistenceContext
    @Getter
    private EntityManager entityManager;
    private static final String GET_COURSE_BY_HOMEWORK_ID = "SELECT c FROM courses c INNER JOIN lessons l on c.id = l.course.id " +
            "INNER JOIN homework h on l.id = h.lesson.id WHERE h.id =: id";
    private static final String GET_ALL_COURSES_BY_INSTRUCTOR_ID = "SELECT c FROM courses c " +
            "INNER JOIN enrollments e on c.id = e.course.id INNER JOIN users u on u.id = e.user.id WHERE u.id =: userId";

    private static final String GET_ALL_USERS_IN_COURSE = "SELECT u FROM courses c INNER JOIN enrollments e on c.id = e.course.id " +
            "INNER JOIN users u on u.id = e.user.id WHERE c.id =: courseId";

    private static final String GET_LESSONS_BY_USER_ID_AND_COURSE_ID = "SELECT l FROM courses c " +
            "INNER JOIN lessons l on c.id = l.course.id INNER JOIN enrollments e on c.id = e.course.id " +
            "WHERE c.id = :courseId AND e.user.id =: userId";

    @Override
    public Optional<Course> findByName(String name) {
        return Optional.ofNullable(getEntityManager().createQuery(SqlQueryConstants.FIND_COURSE_BY_NAME_QUERY, Course.class)
                .setParameter("name", name)
                .getSingleResult());
    }

    @Override
    public boolean exist(String name) {
        Long count = getEntityManager().createQuery(SqlQueryConstants.EXIST_COURSE_BY_NAME_QUERY, Long.class)
                .setParameter("name", name)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public void updateStatus(Long id, CourseStatus status) {
        getEntityManager()
                .createNativeQuery("UPDATE courses SET status =:status WHERE id =:id")
                .setParameter("status", status)
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public List<User> findUsersInCourseByRole(Long id, RoleEnum roleEnum) {
        return getEntityManager().createQuery("SELECT u FROM courses c INNER JOIN enrollments e on c.id = e.course.id INNER JOIN users u on u.id = e.user.id WHERE c.id =: id AND u.role =: role", User.class)
                .setParameter("id", id)
                .setParameter("role", roleEnum)
                .getResultList();
    }

    @Override
    public Optional<List<Lesson>> findAllLessonsInCourse(Long id) {
        return Optional.ofNullable(getEntityManager().createQuery("SELECT l.name FROM courses c INNER JOIN lessons l on c.id = l.course.id WHERE c.id = :id", Lesson.class)
                .setParameter("id", id)
                .getResultList());
    }

    @Override
    public List<Course> findByStartDate(LocalDate currentDate) {
        return getEntityManager().createQuery("SELECT c FROM courses c WHERE c.startDate = :startDate", Course.class)
                .setParameter("startDate", currentDate)
                .getResultList();
    }

    @Override
    public void assignInstructor(Long courseId, Long instructorId) {
        getEntityManager()
                .createNativeQuery("INSERT INTO enrollments (user_id, course_id) VALUES (:instructorId, :courseId )")
                .setParameter("courseId", courseId)
                .setParameter("instructorId", instructorId)
                .executeUpdate();
    }

    @Override
    public Optional<Course> findCourseByHomeworkId(Long homeworkId) {
        return Optional.ofNullable(getEntityManager().createQuery(GET_COURSE_BY_HOMEWORK_ID, Course.class)
                .setParameter("id", homeworkId)
                .getSingleResult());
    }

    @Override
    public Optional<List<Course>> findCoursesByUserId(Long userId) {
        return Optional.ofNullable(getEntityManager().createQuery(GET_ALL_COURSES_BY_INSTRUCTOR_ID, Course.class)
                .setParameter("userId", userId)
                .getResultList());
    }

    @Override
    public List<User> findUsersInCourse(String courseId) {
        return getEntityManager().createQuery(GET_ALL_USERS_IN_COURSE, User.class)
                .setParameter("courseId", courseId)
                .getResultList();
    }

    @Override
    public Optional<List<Lesson>> findAllLessonsByCourseAssignedToUserId(Long studentId, Long courseId) {
        return Optional.ofNullable(getEntityManager().createQuery(GET_LESSONS_BY_USER_ID_AND_COURSE_ID, Lesson.class)
                .setParameter("courseId", courseId)
                .setParameter("userId", studentId)
                .getResultList());
    }

    @Override
    public Class<Course> getEntityClass() {
        return Course.class;
    }
}
