package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.File;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
class LessonRepositoryImplTest {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private HomeworkRepository homeworkRepository;

    @Autowired
    private FileRepository fileRepository;
    @Test
    void testFindAllByCourseId() {
        Course course = Course.builder()
                .name("course")
                .startDate(LocalDate.now())
                .status(CourseStatus.WAIT)
                .build();
        Lesson lesson1 = Lesson.builder()
                .name("lesson1")
                .course(course)
                .build();
        Lesson lesson2 = Lesson.builder()
                .name("lesson2")
                .course(course)
                .build();

        courseRepository.save(course);
        lessonRepository.save(lesson1);
        lessonRepository.save(lesson2);

        List<Lesson> lessons = lessonRepository.findAllByCourseId(course.getId());

        assertEquals(2, lessons.size());
    }

    @Test
    void testFindLessonByHomeworkId() {
        Lesson lesson = Lesson.builder()
                .course(Course.builder()
                        .id(1L)
                        .build())
                .name("lesson1")
                .build();

        lessonRepository.save(lesson);

        File file = File.builder()
                .fileName("file")
                .fileData("testfile".getBytes())
                .build();

        fileRepository.save(file);

        Homework homework = Homework.builder()
                .lesson(lesson)
                .mark(66L)
                .file(file)
                .user(User.builder()
                        .id(1L)
                        .role(RoleEnum.STUDENT)
                        .build())
                .build();

        homeworkRepository.save(homework);

        Optional<Lesson> foundLesson = lessonRepository.findLessonByHomeworkId(homework.getId());

        assertTrue(foundLesson.isPresent());
        assertEquals(lesson, foundLesson.get());
    }

}
