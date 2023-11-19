package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.File;
import edu.sombra.coursemanagementsystem.repository.FileRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.Generated;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class FileRepositoryImpl implements FileRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Generated
    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Generated
    @Override
    public Class<File> getEntityClass() {
        return File.class;
    }
}
