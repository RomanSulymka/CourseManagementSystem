package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.File;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class FileRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FileRepository fileRepository;

    @Test
    void testFindFileNameById() {
        File file = new File();
        file.setFileName("test.txt");
        file.setFileData("testtesttest".getBytes());
        entityManager.persist(file);
        entityManager.flush();

        String fileName = fileRepository.findFileNameById(file.getId());

        assertNotNull(fileName);
        assertEquals("test.txt", fileName);
    }
}
