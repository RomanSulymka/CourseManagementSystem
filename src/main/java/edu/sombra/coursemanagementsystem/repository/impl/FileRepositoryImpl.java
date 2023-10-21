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

    @Override
    public byte[] findDataById(Long fileId) {
        return getEntityManager().createQuery("SELECT f.fileData FROM files f WHERE id =: id", byte[].class)
                .setParameter("id", fileId)
                .getSingleResult();
    }

    @Override
    public String findFileNameById(Long fileId) {
        return getEntityManager().createQuery("SELECT f.fileName FROM files f WHERE id =: id", String.class)
                .setParameter("id", fileId)
                .getSingleResult();
    }

    @Override
    public File findFileByName(String fileName) {
        return getEntityManager().createQuery("SELECT f FROM files f WHERE f.fileName =: fileName", File.class)
                .setParameter("fileName", fileName)
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
