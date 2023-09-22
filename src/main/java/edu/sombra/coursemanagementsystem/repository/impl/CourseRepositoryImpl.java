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
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class CourseRepositoryImpl implements CourseRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Course> findByName(String name) {
        return Optional.ofNullable(entityManager().createQuery(SqlQueryConstants.FIND_COURSE_BY_NAME_QUERY, Course.class)
                .setParameter("name", name)
                .getSingleResult());
    }

    @Override
    public boolean exist(String name) {
        Long count = entityManager().createQuery(SqlQueryConstants.EXIST_COURSE_BY_NAME_QUERY, Long.class)
                .setParameter("name", name)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public void updateStatus(Long id, CourseStatus status) {
        entityManager()
                .createNativeQuery("UPDATE courses SET status =:status WHERE id =:id")
                .setParameter("status", status)
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public List<User> findUsersInCourseByRole(Long id, RoleEnum roleEnum) {
        return entityManager().createQuery("SELECT u FROM courses c INNER JOIN enrollments e on c.id = e.course.id INNER JOIN users u on u.id = e.user.id WHERE c.id =: id AND u.role =: role", User.class)
                .setParameter("id", id)
                .setParameter("role", roleEnum)
                .getResultList();
    }

    @Override
    public Optional<List<Lesson>> findAllLessonsInCourse(Long id) {
        return Optional.ofNullable(entityManager().createQuery("SELECT l.name FROM courses c INNER JOIN lessons l on c.id = l.course.id WHERE c.id = : id", Lesson.class)
                .setParameter("id", id)
                .getResultList());
    }

    @Override
    public List<Course> findByStartDate(LocalDate currentDate) {
        return entityManager().createQuery("SELECT c FROM courses c WHERE c.startDate = :startDate", Course.class)
                .setParameter("startDate", currentDate)
                .getResultList();
    }

    @Override
    public void assignInstructor(Long courseId, Long instructorId) {
        entityManager()
                .createNativeQuery("INSERT INTO enrollments (user_id, course_id) VALUES (:instructorId, :courseId )")
                .setParameter("courseId", courseId)
                .setParameter("instructorId", instructorId)
                .executeUpdate();
    }

    @Override
    public EntityManager entityManager() {
        return entityManager;
    }

    @Override
    public Class<Course> getEntityClass() {
        return Course.class;
    }
}
