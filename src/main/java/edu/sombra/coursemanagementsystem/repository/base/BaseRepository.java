package edu.sombra.coursemanagementsystem.repository.base;

import jakarta.persistence.EntityManager;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;


@NoRepositoryBean
public interface BaseRepository<T, ID> {

    EntityManager getEntityManager();

    Class<T> getEntityClass();

    default T save(T entity) {
        getEntityManager().persist(entity);
        return entity;
    }

    default List<T> saveAll(List<T> entities) {
        for (T entity : entities) {
            getEntityManager().persist(entity);
        }
        return entities;
    }

    default T update(T entity) {
        return getEntityManager().merge(entity);
    }

    default void delete(T entity) {
        getEntityManager().remove(entity);
    }

    default Optional<T> findById(ID id) {
        T entity = getEntityManager().find(getEntityClass(), id);
        return Optional.ofNullable(entity);
    }

    default List<T> findAll() {
        return getEntityManager()
                .createQuery("SELECT e FROM " + getEntityClass().getName() + " e", getEntityClass())
                .getResultList();
    }

    default boolean existsById(ID id) {
        return findById(id).isPresent();
    }
}
