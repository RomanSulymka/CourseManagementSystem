package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@AllArgsConstructor
@Repository
public class HomeworkRepositoryImpl implements HomeworkRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public EntityManager entityManager() {
        return entityManager;
    }

    @Override
    public Class<Homework> getEntityClass() {
        return Homework.class;
    }
}
