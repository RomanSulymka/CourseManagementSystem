package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.query.SqlQueryConstants;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CourseRepositoryImpl implements CourseRepository{
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
    public EntityManager entityManager() {
        return entityManager;
    }

    @Override
    public Class<Course> getEntityClass() {
        return Course.class;
    }
}
