package edu.sombra.coursemanagementsystem.repository.base.impl;

import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class BaseRepositoryImpl<T, ID> implements BaseRepository<T, ID> {
    @PersistenceContext
    private EntityManager entityManager;
    private final Class<T> entityClass;

    public BaseRepositoryImpl(EntityManager entityManager, Class<T> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }

    @Override
    public EntityManager entityManager() {
        return entityManager;
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }
}
