package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.query.SqlQueryConstants;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.repository.base.impl.BaseRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CourseRepositoryImpl extends BaseRepositoryImpl<Course, Long> implements CourseRepository{
    @PersistenceContext
    private EntityManager entityManager;

    public CourseRepositoryImpl() {
        super(Course.class);
    }

    @Override
    public Optional<Course> findByName(String name) {
        return Optional.ofNullable(entityManager.createQuery(SqlQueryConstants.FIND_COURSE_BY_NAME_QUERY, Course.class)
                .setParameter("name", name)
                .getSingleResult());
    }

    @Override
    public boolean exist(String name) {
        Long count = entityManager.createQuery(SqlQueryConstants.EXIST_COURSE_BY_NAME_QUERY, Long.class)
                .setParameter("name", name)
                .getSingleResult();
        return count != null && count > 0;
    }
}
