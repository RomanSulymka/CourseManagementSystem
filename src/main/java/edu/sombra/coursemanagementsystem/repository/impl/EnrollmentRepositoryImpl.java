package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Enrollment;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.repository.EnrollmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import lombok.Generated;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EnrollmentRepositoryImpl implements EnrollmentRepository {
    @PersistenceContext
    private EntityManager entityManager;
    public static final String GET_COURSES_BY_USER_ID = "SELECT c FROM enrollments e INNER JOIN courses c on c.id = e.course.id WHERE e.user.id =:id";
    public static final String GET_COURSES_BY_ENROLLMENT_ID = "SELECT e.course FROM enrollments e INNER JOIN users u on u.id = e.user.id where e.id =: id";
    public static final String GET_ASSIGNED_INSTRUCTOR_FOR_COURSE = "SELECT u FROM enrollments e INNER JOIN users u on u.id = e.user.id WHERE e.course.id =: id AND u.role = 'INSTRUCTOR'";
    public static final String GET_USER_BY_ENROLLMENT = "SELECT u FROM enrollments e INNER JOIN users u on u.id = e.user.id WHERE e.id =: id";
    public static final String GET_ENROLLMENT_BY_COURSE_NAME = "select c.name, u.firstName, u.lastName, u.role, u.email from enrollments e inner join courses c on c.id = e.course.id INNER JOIN users u on u.id = e.user.id where c.name = :name";
    public static final String GET_NUMBER_OF_USER_COURSES = "SELECT count(e.course.id) FROM enrollments e WHERE e.user.id =: userId";
    public static final String IS_USER_ALREADY_ASSIGNED_FOR_COURSE_QUERY = "SELECT COUNT(e) FROM enrollments e WHERE e.course = :course AND e.user = :user";

    @Override
    public boolean isUserAssignedToCourse(Course course, User user) {
        return !entityManager.createQuery(
                        IS_USER_ALREADY_ASSIGNED_FOR_COURSE_QUERY, Long.class)
                .setParameter("user", user)
                .setParameter("course", course)
                .getSingleResult()
                .equals(0L);
    }

    @Override
    public List<Tuple> findEnrollmentByCourseName(String name) {
        return getEntityManager().createQuery(GET_ENROLLMENT_BY_COURSE_NAME, Tuple.class)
                .setParameter("name", name)
                .getResultList();
    }

    @Override
    public Long getUserRegisteredCourseCount(Long userId) {
        return getEntityManager().createQuery(GET_NUMBER_OF_USER_COURSES, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    @Override
    public List<Course> findCoursesByUserId(Long id) {
        return getEntityManager().createQuery(GET_COURSES_BY_USER_ID, Course.class)
                .setParameter("id", id)
                .getResultList();
    }

    @Override
    public Course findCourseByEnrollmentId(Long id) {
        return getEntityManager().createQuery(GET_COURSES_BY_ENROLLMENT_ID, Course.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    @Override
    public List<User> findAssignedInstructorsForCourse(Long id) {
        return getEntityManager().createQuery(GET_ASSIGNED_INSTRUCTOR_FOR_COURSE, User.class)
                .setParameter("id", id)
                .getResultList();
    }

    @Override
    public User findUserByEnrollmentId(Long id) {
        try {
            return getEntityManager().createQuery(GET_USER_BY_ENROLLMENT, User.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            throw new EntityNotFoundException("Enrollment not found!");
        }
    }

    @Generated
    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Generated
    @Override
    public Class<Enrollment> getEntityClass() {
        return Enrollment.class;
    }
}
