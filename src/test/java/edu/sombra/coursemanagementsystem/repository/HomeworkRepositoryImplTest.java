package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Enrollment;
import edu.sombra.coursemanagementsystem.entity.File;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
class HomeworkRepositoryImplTest {

    @Autowired
    private HomeworkRepository homeworkRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Test
    @Transactional
    void testSetMark() {
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

        homeworkRepository.setMark(homework.getId(), 90L);

        entityManager.flush();

        entityManager.clear();

        Homework updatedHomework = homeworkRepository.findById(homework.getId()).orElse(null);

        assertNotNull(updatedHomework);
        assertEquals(90L, updatedHomework.getMark());
    }

    @Test
    @Transactional
    void testCalculateAverageHomeworksMarkByUserId() {
        User user = User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("user@example.com")
                .role(RoleEnum.STUDENT)
                .build();

        userRepository.save(user);

        Course course = Course.builder()
                .name("Java Programming")
                .status(CourseStatus.STOP)
                .startDate(LocalDate.now())
                .build();

        courseRepository.save(course);

        Lesson lesson1 = Lesson.builder()
                .course(course)
                .name("lesson1")
                .build();

        Lesson lesson2 = Lesson.builder()
                .course(course)
                .name("lesson2")
                .build();

        lessonRepository.saveAll(List.of(lesson1, lesson2));

        File file = File.builder()
                .fileName("file1")
                .fileData("testfile".getBytes())
                .build();

        File file2 = File.builder()
                .fileName("file2")
                .fileData("testfile2".getBytes())
                .build();

        fileRepository.saveAll(List.of(file, file2));

        Homework homework1 = Homework.builder()
                .lesson(lesson1)
                .mark(89L)
                .file(file)
                .user(user)
                .build();

        Homework homework2 = Homework.builder()
                .lesson(lesson2)
                .mark(90L)
                .file(file2)
                .user(user)
                .build();

        homeworkRepository.saveAll(List.of(homework1, homework2));

        Double averageMark = homeworkRepository.calculateAverageHomeworksMarkByUserId(user.getId(), course.getId());

        assertNotNull(averageMark);
        assertEquals((homework1.getMark() + homework2.getMark()) / 2.0, averageMark, 0.01);

    }

    @Test
    void testFindByUserAndLessonId() {
        User user = User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("user@example.com")
                .role(RoleEnum.STUDENT)
                .build();

        userRepository.save(user);

        Lesson lesson = Lesson.builder()
                .course(Course.builder()
                        .id(1L)
                        .build())
                .name("lesson1")
                .build();

        lessonRepository.save(lesson);

        File file = File.builder()
                .fileName("file1")
                .fileData("testfile".getBytes())
                .build();

        fileRepository.save(file);

        Homework homework = Homework.builder()
                .lesson(lesson)
                .mark(89L)
                .file(file)
                .user(user)
                .build();

        homeworkRepository.save(homework);

        Optional<Homework> foundHomework = homeworkRepository.findByUserAndLessonId(user.getId(), lesson.getId());

        assertTrue(foundHomework.isPresent());
        assertEquals(homework, foundHomework.get());
    }

    @Test
    @Transactional
    void testAssignUserForLesson() {
        User user = User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("user@example.com")
                .role(RoleEnum.STUDENT)
                .build();

        Lesson lesson = Lesson.builder()
                .course(Course.builder()
                        .id(1L)
                        .build())
                .name("lesson1")
                .build();

        userRepository.save(user);
        lessonRepository.save(lesson);

        homeworkRepository.assignUserForLesson(user.getId(), lesson.getId());

        entityManager.flush();

        Homework assignedHomework = homeworkRepository.findByUserAndLessonId(user.getId(), lesson.getId()).orElse(null);

        assertNotNull(assignedHomework);
    }

    @Test
    void testFindHomeworksByCourse() {
        User user = User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("user@example.com")
                .role(RoleEnum.STUDENT)
                .build();

        userRepository.save(user);

        Course course = Course.builder()
                .name("Java Programming")
                .status(CourseStatus.STOP)
                .startDate(LocalDate.now())
                .build();

        courseRepository.save(course);

        Lesson lesson1 = Lesson.builder()
                .course(course)
                .name("lesson1")
                .build();

        Lesson lesson2 = Lesson.builder()
                .course(course)
                .name("lesson1")
                .build();

        lessonRepository.saveAll(List.of(lesson1, lesson2));

        File file = File.builder()
                .fileName("file1")
                .fileData("testfile".getBytes())
                .build();

        File file2 = File.builder()
                .fileName("file2")
                .fileData("testfile2".getBytes())
                .build();

        fileRepository.saveAll(List.of(file, file2));

        Homework homework1 = Homework.builder()
                .lesson(lesson1)
                .mark(89L)
                .file(file)
                .user(user)
                .build();

        Homework homework2 = Homework.builder()
                .lesson(lesson2)
                .mark(89L)
                .file(file2)
                .user(user)
                .build();

        homeworkRepository.saveAll(List.of(homework1, homework2));

        List<Homework> foundHomeworks = homeworkRepository.findHomeworksByCourse(course.getId());

        assertEquals(2, foundHomeworks.size());
        assertTrue(foundHomeworks.contains(homework1));
        assertTrue(foundHomeworks.contains(homework2));
    }

    @Test
    void testIsUserUploadedHomework() {
        User user = User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("user@example.com")
                .role(RoleEnum.STUDENT)
                .build();

        userRepository.save(user);

        Lesson lesson = Lesson.builder()
                .course(Course.builder()
                        .id(1L)
                        .build())
                .name("lesson1")
                .build();

        lessonRepository.save(lesson);

        File file = File.builder()
                .fileName("file1")
                .fileData("testfile".getBytes())
                .build();

        fileRepository.save(file);

        Homework homework = Homework.builder()
                .lesson(lesson)
                .mark(89L)
                .file(file)
                .user(user)
                .build();

        homeworkRepository.save(homework);

        userRepository.save(user);
        fileRepository.save(file);
        homeworkRepository.save(homework);

        boolean isUploaded = homeworkRepository.isUserUploadedHomework(file.getId(), user.getId());

        assertTrue(isUploaded);
    }

    @Test
    void testFindAllByUser() {
        User user = User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("user@example.com")
                .role(RoleEnum.STUDENT)
                .build();

        userRepository.save(user);

        Course course = Course.builder()
                .name("Java Programming")
                .status(CourseStatus.STOP)
                .startDate(LocalDate.now())
                .build();

        courseRepository.save(course);

        Lesson lesson1 = Lesson.builder()
                .course(course)
                .name("lesson1")
                .build();

        Lesson lesson2 = Lesson.builder()
                .course(course)
                .name("lesson1")
                .build();

        lessonRepository.saveAll(List.of(lesson1, lesson2));

        File file = File.builder()
                .fileName("file1")
                .fileData("testfile".getBytes())
                .build();

        File file2 = File.builder()
                .fileName("file2")
                .fileData("testfile2".getBytes())
                .build();

        fileRepository.saveAll(List.of(file, file2));

        Homework homework1 = Homework.builder()
                .lesson(lesson1)
                .mark(89L)
                .file(file)
                .user(user)
                .build();

        Homework homework2 = Homework.builder()
                .lesson(lesson2)
                .mark(89L)
                .file(file2)
                .user(user)
                .build();

        userRepository.save(user);
        homeworkRepository.saveAll(List.of(homework1, homework2));

        List<Homework> foundHomework = homeworkRepository.findAllByUser(user.getId());

        assertEquals(2, foundHomework.size());
        assertTrue(foundHomework.contains(homework1));
        assertTrue(foundHomework.contains(homework2));
    }

    @Test
    void testFindAllHomeworksWithInstructorAccess() {
        User instructor = User.builder()
                .lastName("Instructor")
                .firstName("Test")
                .password("123")
                .email("instructor@example.com")
                .role(RoleEnum.INSTRUCTOR)
                .build();

        userRepository.save(instructor);

        User student = User.builder()
                .lastName("Student")
                .firstName("Test")
                .password("123")
                .email("student@example.com")
                .role(RoleEnum.STUDENT)
                .build();

        userRepository.save(student);

        Course course = Course.builder()
                .name("Test Course")
                .startDate(LocalDate.of(2023, 1, 1))
                .status(CourseStatus.STARTED)
                .build();

        Enrollment studentEnrollment = Enrollment.builder()
                .user(student)
                .course(course)
                .build();

        Enrollment instructorEnrollment = Enrollment.builder()
                .user(instructor)
                .course(course)
                .build();

        courseRepository.save(course);
        enrollmentRepository.save(studentEnrollment);
        enrollmentRepository.save(instructorEnrollment);

        File file = File.builder()
                .fileName("testfile")
                .fileData("testdata".getBytes())
                .build();

        fileRepository.save(file);

        Homework homework = Homework.builder()
                .user(student)
                .lesson(Lesson.builder().id(1L).course(course).build())
                .mark(85L)
                .file(file)
                .build();

        homeworkRepository.save(homework);

        List<Homework> homeworkList = homeworkRepository.findAllHomeworksWithInstructorAccess(instructor.getId());

        assertFalse(homeworkList.isEmpty());
        assertEquals(1, homeworkList.size());
        assertEquals(homework, homeworkList.get(0));
    }
}
