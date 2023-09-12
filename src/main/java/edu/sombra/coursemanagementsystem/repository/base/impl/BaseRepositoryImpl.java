package edu.sombra.coursemanagementsystem.repository.base.impl;

import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class BaseRepositoryImpl<T, ID> implements BaseRepository<T, ID> {
    @PersistenceContext
    private EntityManager entityManager;
    private final Class<T> entityClass;

    @Autowired
    public BaseRepositoryImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public T create(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Override
    public T update(T entity) {
        return entityManager.merge(entity);
    }

    @Override
    public boolean delete(ID id) {
        T entity = entityManager.find(entityClass, id);
        if (entity != null) {
            entityManager.remove(entity);
            return true;
        }
        return false;
    }

    @Override
    public T findById(ID id) {
        return entityManager.find(entityClass, id);
    }

    @Override
    public List<T> findAll() {
        return entityManager
                .createQuery("SELECT e FROM " + entityClass.getName() + " e", entityClass)
                .getResultList();
    }
}
