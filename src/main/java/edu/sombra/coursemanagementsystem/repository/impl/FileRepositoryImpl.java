package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.File;
import edu.sombra.coursemanagementsystem.repository.FileRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class FileRepositoryImpl implements FileRepository {
    @PersistenceContext
    private EntityManager entityManager;
    public static final String GET_FILE_NAME_BY_ID = "SELECT f.fileName FROM files f WHERE id =: id";

    @Override
    public String findFileNameById(Long fileId) {
        return getEntityManager().createQuery(GET_FILE_NAME_BY_ID, String.class)
                .setParameter("id", fileId)
                .getSingleResult();
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Class<File> getEntityClass() {
        return File.class;
    }
}
