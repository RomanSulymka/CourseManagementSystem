package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Enrollment;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.repository.EnrollmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public class EnrollmentRepositoryImpl implements EnrollmentRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addInstructorToCourse(Enrollment enrollment) {
        entityManager.persist(enrollment);
    }

    @Override
    public void saveAll(Iterable<Enrollment> enrollments) {
        for (Enrollment enrollment : enrollments) {
            entityManager.persist(enrollment);
        }
    }

    @Override
    public boolean areUsersAssignedToCourse(Course course, List<User> users) {
        return !entityManager.createQuery(
                        "SELECT COUNT(e) FROM enrollments e WHERE e.course = :course AND e.user IN :users", Long.class)
                .setParameter("course", course)
                .setParameter("users", users)
                .getSingleResult()
                .equals(0L);
    }
}
