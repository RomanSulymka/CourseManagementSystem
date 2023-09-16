package edu.sombra.coursemanagementsystem.repository.base;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public interface BaseRepository<T, ID> {

    EntityManager entityManager();

    Class<T> getEntityClass();

    default T save(T entity) {
        entityManager().persist(entity);
        return entity;
    }

    default T update(T entity) {
        return entityManager().merge(entity);
    }

    default void delete(T entity) {
        entityManager().remove(entity);
    }

    default Optional<T> findById(ID id) {
        T entity = entityManager().find(getEntityClass(), id);
        return Optional.ofNullable(entity);
    }

    default List<T> findAll() {
        return entityManager()
                .createQuery("SELECT e FROM " + getEntityClass().getName() + " e", getEntityClass())
                .getResultList();
    }

    default boolean existsById(ID id) {
        return findById(id).isPresent();
    }
}
