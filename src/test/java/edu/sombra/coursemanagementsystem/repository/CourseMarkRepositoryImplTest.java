package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.CourseMark;
import edu.sombra.coursemanagementsystem.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
class CourseMarkRepositoryImplTest {

    @Autowired
    private CourseMarkRepository courseMarkRepository;

    @Test
    @Transactional
    void testUpsertCourseMark() {
        User user = User.builder().id(1L).build();
        Course course = Course.builder().id(2L).build();

        CourseMark courseMark = CourseMark.builder()
                .user(user)
                .course(course)
                .totalScore(new BigDecimal("95.50"))
                .passed(true)
                .build();

        courseMarkRepository.upsert(courseMark);
        CourseMark retrievedCourseMark = courseMarkRepository.findCourseMarkByUserIdAndCourseId(user.getId(), course.getId()).orElse(null);
        assertNotNull(retrievedCourseMark, "Upserted CourseMark should be retrieved");
        assertEquals(courseMark.getTotalScore(), retrievedCourseMark.getTotalScore(), "Total scores should match");
        assertEquals(courseMark.getPassed(), retrievedCourseMark.getPassed(), "Passed status should match");
    }

    @Test
    @Transactional
    void testFindCourseMarkByUserIdAndCourseId() {
        User user = User.builder().id(1L).build();
        Course course = Course.builder().id(2L).build();

        CourseMark courseMark = CourseMark.builder()
                .user(user)
                .course(course)
                .totalScore(new BigDecimal("95.50"))
                .passed(true)
                .build();

        courseMarkRepository.upsert(courseMark);
        Optional<CourseMark> foundCourseMark = courseMarkRepository.findCourseMarkByUserIdAndCourseId(user.getId(), course.getId());

        assertTrue(foundCourseMark.isPresent(), "CourseMark should be found");
        assertEquals(courseMark.getTotalScore(), foundCourseMark.get().getTotalScore(), "Total scores should match");
        assertEquals(courseMark.getPassed(), foundCourseMark.get().getPassed(), "Passed status should match");

        Optional<CourseMark> notFoundCourseMark = courseMarkRepository.findCourseMarkByUserIdAndCourseId(999L, 999L);
        assertTrue(notFoundCourseMark.isEmpty(), "CourseMark should not be found for non-existent user and course");
    }
}