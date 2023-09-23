package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.dto.enrollment.RemoveInstructorDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Enrollment;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.repository.EnrollmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EnrollmentRepositoryImpl implements EnrollmentRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public static final String GET_ENROLLMENT_BY_COURSE_NAME = "select c.name, u.firstName, u.lastName, u.role, u.email from enrollments e inner join courses c on c.id = e.course.id INNER JOIN users u on u.id = e.user.id where c.name = :name";
    public static final String GET_NUMBER_OF_USER_COURSES = "SELECT count(e.course.id) FROM enrollments e WHERE e.user.id =: userId";
    public static final String IS_USERS_ALREADY_ASSIGNED_FOR_COURSE_QUERY = "SELECT COUNT(e) FROM enrollments e WHERE e.course = :course AND e.user IN :users";
    public static final String IS_USER_ALREADY_ASSIGNED_FOR_COURSE_QUERY = "SELECT COUNT(e) FROM enrollments e WHERE e.course = :course AND e.user = :user";

    @Override
    public void assignUserToCourse(Enrollment enrollment) {
        entityManager().persist(enrollment);
    }

    @Override
    public void saveAll(Iterable<Enrollment> enrollments) {
        for (Enrollment enrollment : enrollments) {
            entityManager().persist(enrollment);
        }
    }

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
    public boolean areUsersAssignedToCourse(Course course, List<User> users) {
        return !entityManager().createQuery(
                        IS_USERS_ALREADY_ASSIGNED_FOR_COURSE_QUERY, Long.class)
                .setParameter("course", course)
                .setParameter("users", users)
                .getSingleResult()
                .equals(0L);
    }

    @Override
    public Enrollment findByInstructorId(RemoveInstructorDTO dto) {
        return null;
    }

    @Override
    public List<Tuple> findEnrollmentByCourseName(String name) {
        return entityManager().createQuery(GET_ENROLLMENT_BY_COURSE_NAME, Tuple.class)
                .setParameter("name", name)
                .getResultList();
    }

    @Override
    public Long getUserRegisteredCourseCount(Long userId) {
        return entityManager().createQuery(GET_NUMBER_OF_USER_COURSES, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    @Override
    public List<String> findCoursesByUserId(Long id) {
        return entityManager().createQuery("SELECT c.name FROM enrollments e INNER JOIN courses c on c.id = e.course.id WHERE e.user.id =:id", String.class)
                .setParameter("id", id)
                .getResultList();
    }

    @Override
    public Course findCourseByEnrollmentId(Long id) {
        return entityManager().createQuery("SELECT e.course FROM enrollments e INNER JOIN users u on u.id = e.user.id where e.id =: id", Course.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    @Override
    public List<User> findAssignedInstructorsForCourse(Long id) {
        return entityManager().createQuery("SELECT u FROM enrollments e INNER JOIN users u on u.id = e.user.id WHERE e.course.id =: id AND u.role = 'INSTRUCTOR'", User.class)
                .setParameter("id", id)
                .getResultList();
    }

    @Override
    public User findUserByEnrollmentId(Long id) {
        return entityManager().createQuery("SELECT u FROM enrollments e INNER JOIN users u on u.id = e.user.id WHERE e.id =: id", User.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    @Override
    public EntityManager entityManager() {
        return entityManager;
    }

    @Override
    public Class<Enrollment> getEntityClass() {
        return Enrollment.class;
    }
}
