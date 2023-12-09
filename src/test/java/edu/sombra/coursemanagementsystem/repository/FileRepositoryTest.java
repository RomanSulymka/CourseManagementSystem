package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.File;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
class FileRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

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
        assertThrows(IllegalArgumentException.class, () -> fileRepository.findById(null));
    }

    @Test
    void testFindFileByIdWithNegativeId() {
        Long negativeId = -1L;

        assertThrows(NoSuchElementException.class, () -> fileRepository.findById(negativeId).orElseThrow());
    }
}
