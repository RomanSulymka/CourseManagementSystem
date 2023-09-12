package edu.sombra.coursemanagementsystem.repository.base;

import java.util.List;

public interface BaseRepository<T, ID> {
    T create(T entity);
    T update(T entity);
    boolean delete(ID id);
    T findById(ID id);
    List<T> findAll();
}
