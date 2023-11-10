package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.File;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Transactional
class FileRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FileRepository fileRepository;

    @Test
    void testSaveAndFindFileById() {
        File file = new File();
        file.setFileName("test.txt");
        file.setFileData("testtesttest".getBytes());
        fileRepository.save(file);
        entityManager.flush();

        File receivedFile = fileRepository.findById(file.getId()).orElse(null);

        assertNotNull(receivedFile);
        assertEquals("test.txt", receivedFile.getFileName());
    }

    @Test
    void testFindFileByIdWithNullId() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> fileRepository.findById(null));
    }

    @Test
    void testFindFileByIdWithNegativeId() {
        Long negativeId = -1L;

        assertThrows(NoSuchElementException.class, () -> fileRepository.findById(negativeId).orElseThrow());
    }
}
