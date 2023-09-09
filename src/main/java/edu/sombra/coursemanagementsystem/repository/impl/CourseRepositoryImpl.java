package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.query.SqlQueryConstants;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CourseRepositoryImpl implements CourseRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Course> findByName(String name) {
        return Optional.ofNullable(entityManager.createQuery(SqlQueryConstants.FIND_COURSE_BY_NAME_QUERY, Course.class)
                .setParameter("name", name)
                .getSingleResult());
    }

    @Override
    public Course save(Course course) {
        entityManager.persist(course);
        return course;
    }

    @Override
    public Course findById(Long courseId) {
        return entityManager.find(Course.class, courseId);
    }

    @Override
    public Optional<Course> updateCourse(Long courseId, String name) {
        Course course = findById(courseId);
        course.setName(name);
        entityManager.persist(course);
        return findByName(name);
    }

    @Override
    public boolean deleteCourseById(Long id) {
        Course course = findById(id);
        if (course != null) {
            entityManager.remove(course);
            return true;
        }
        return false;
    }
}
