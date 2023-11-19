package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.CourseFeedback;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.repository.impl.CourseFeedbackRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
class CourseFeedbackRepositoryImplTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseFeedbackRepositoryImpl courseFeedbackRepository;

    @Test
    void testFindFeedback() {
        Long studentId = 1L;
        Long courseId = 2L;

        CourseFeedback feedback = new CourseFeedback();
        feedback.setFeedbackText("test");
        feedback.setStudent(User.builder().id(studentId).role(RoleEnum.STUDENT).build());
        feedback.setCourse(Course.builder().id(courseId).build());
        feedback.setInstructor(User.builder().id(12L).role(RoleEnum.INSTRUCTOR).build());

        entityManager.persistAndFlush(feedback);

        Optional<CourseFeedback> foundFeedback = courseFeedbackRepository.findFeedback(studentId, courseId);

        assertTrue(foundFeedback.isPresent());
    }

    @Test
    void testFindFeedback_NotFound() {
        Long nonExistingStudentId = 100L;
        Long courseId = 2L;

        Optional<CourseFeedback> foundFeedback = courseFeedbackRepository.findFeedback(nonExistingStudentId, courseId);

        assertFalse(foundFeedback.isPresent());
    }
}
