package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Enrollment;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.repository.impl.CourseRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
class CourseRepositoryImplTest {

    @Autowired
    private CourseRepositoryImpl courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private HomeworkRepository homeworkRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    void testFindByName() {
        String courseName = "Test Course";
        Course course = Course.builder()
                .name(courseName)
                .startDate(LocalDate.now())
                .status(CourseStatus.WAIT)
                .build();

        courseRepository.save(course);
        Optional<Course> foundCourse = courseRepository.findByName(courseName);

        assertTrue(foundCourse.isPresent(), "Course should be found by name");
        assertEquals(courseName, foundCourse.get().getName(), "Course names should match");
    }

    @Test
    @Transactional
    void testExist() {
        String existingCourseName = "Existing Course";
        Course existingCourse = Course.builder()
                .name(existingCourseName)
                .status(CourseStatus.WAIT)
                .startDate(LocalDate.now())
                .build();

        courseRepository.save(existingCourse);

        boolean doesExist = courseRepository.exist(existingCourseName);

        assertTrue(doesExist, "Course should exist");
    }

    @Test
    void testFindUsersInCourseByRole() {
        Course course = Course.builder()
                .name("Test Course")
                .startDate(LocalDate.now())
                .status(CourseStatus.WAIT)
                .build();

        courseRepository.save(course);

        User instructor = User.builder()
                .id(3L)
                .email("instructor@gmail.com")
                .role(RoleEnum.INSTRUCTOR)
                .build();

        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .user(instructor)
                .build();

        enrollmentRepository.save(enrollment);

        User student1 = userRepository.save(User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("user1@example.com")
                .role(RoleEnum.STUDENT)
                .build());

        User student2 = userRepository.save(User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("user2@example.com")
                .role(RoleEnum.STUDENT)
                .build());

        enrollmentRepository.save(Enrollment.builder()
                .course(course)
                .user(student1)
                .build());

        enrollmentRepository.save(Enrollment.builder()
                .course(course)
                .user(student2)
                .build());

        List<User> instructorsInCourse = courseRepository.findUsersInCourseByRole(course.getId(), RoleEnum.INSTRUCTOR);
        List<User> studentsInCourse = courseRepository.findUsersInCourseByRole(course.getId(), RoleEnum.STUDENT);

        assertEquals(1, instructorsInCourse.size(), "There should be one instructor in the course");
        assertEquals(instructor.getUsername(), instructorsInCourse.get(0).getUsername(), "Instructor username should match");

        assertEquals(2, studentsInCourse.size(), "There should be two students in the course");
        assertTrue(studentsInCourse.stream().anyMatch(user -> user.getEmail().equals(student1.getEmail())), "Student1 should be in the course");
        assertTrue(studentsInCourse.stream().anyMatch(user -> user.getEmail().equals(student2.getEmail())), "Student2 should be in the course");
    }

    @Test
    @Transactional
    void testFindCourseByHomeworkId() {
        Course course = Course.builder()
                .name("Test Course")
                .startDate(LocalDate.now())
                .status(CourseStatus.WAIT)
                .build();

        courseRepository.save(course);

        Lesson lesson = Lesson.builder().course(course).name("name").build();
        lessonRepository.save(lesson);

        Homework homework = Homework.builder().lesson(lesson).user(User.builder().id(1L).build()).build();
        homeworkRepository.save(homework);

        Optional<Course> foundCourse = courseRepository.findCourseByHomeworkId(homework.getId());

        assertTrue(foundCourse.isPresent(), "Course should be found by homeworkId");
        assertEquals(course.getName(), foundCourse.get().getName(), "Course names should match");
    }

    @Test
    @Transactional
    void testIsUserAssignedToCourse() {
        Course course = Course.builder()
                .name("Test Course")
                .startDate(LocalDate.now())
                .status(CourseStatus.WAIT)
                .build();

        courseRepository.save(course);

        User user = User.builder().id(1L).email("user@example.com").build();
        enrollmentRepository.save(Enrollment.builder()
                .course(course)
                .user(user)
                .build());

        boolean isUserAssigned = courseRepository.isUserAssignedToCourse(user.getId(), course.getId());

        assertTrue(isUserAssigned, "User should be assigned to the course");
    }

    @Test
    @Transactional
    void testFindCoursesByUserId() {
        Course course1 = Course.builder()
                .name("Test Course1")
                .startDate(LocalDate.now())
                .status(CourseStatus.WAIT)
                .build();

        Course course2 = Course.builder()
                .name("Test Course2")
                .startDate(LocalDate.now())
                .status(CourseStatus.WAIT)
                .build();

        courseRepository.save(course1);
        courseRepository.save(course2);

        User user = User.builder()
                .email("usertest@example.com")
                .role(RoleEnum.STUDENT)
                .firstName("firstName")
                .lastName("lastName")
                .password("1234").build();

        userRepository.save(user);

        enrollmentRepository.save(Enrollment.builder()
                .course(course1)
                .user(user)
                .build());

        enrollmentRepository.save(Enrollment.builder()
                .course(course2)
                .user(user)
                .build());

        Optional<List<Course>> foundCourses = courseRepository.findCoursesByUserId(user.getId());

        assertTrue(foundCourses.isPresent(), "Courses should be found by userId");
        assertEquals(2, foundCourses.get().size(), "Number of courses should match");
    }

    @Test
    @Transactional
    void testFindUsersInCourse() {
        Course course = Course.builder()
                .name("Test Course")
                .startDate(LocalDate.now())
                .status(CourseStatus.WAIT)
                .build();

        courseRepository.save(course);

        User user1 = User.builder()
                .email("usertest1@example.com")
                .role(RoleEnum.STUDENT)
                .firstName("firstName")
                .lastName("lastName")
                .password("1234").build();

        User user2 = User.builder()
                .email("usertest2@example.com")
                .role(RoleEnum.STUDENT)
                .firstName("firstName")
                .lastName("lastName")
                .password("1234").build();

        userRepository.save(user1);
        userRepository.save(user2);

        enrollmentRepository.save(Enrollment.builder()
                .course(course)
                .user(user1)
                .build());

        enrollmentRepository.save(Enrollment.builder()
                .course(course)
                .user(user2)
                .build());
        List<User> usersInCourse = courseRepository.findUsersInCourse(course.getId());

        assertEquals(2, usersInCourse.size(), "Number of users in the course should match");
    }

    @Test
    @Transactional
    void testFindAllLessonsByCourseAssignedToUserId() {
        Course course = Course.builder()
                .name("Test Course")
                .startDate(LocalDate.now())
                .status(CourseStatus.WAIT)
                .build();

        courseRepository.save(course);

        User user = User.builder()
                .email("usertest@example.com")
                .role(RoleEnum.STUDENT)
                .firstName("firstName")
                .lastName("lastName")
                .password("1234").build();

        userRepository.save(user);

        enrollmentRepository.save(Enrollment.builder()
                .course(course)
                .user(user)
                .build());

        Lesson lesson1 = Lesson.builder().course(course).name("lesson1").build();
        Lesson lesson2 = Lesson.builder().course(course).name("lesson2").build();
        lessonRepository.save(lesson1);
        lessonRepository.save(lesson2);

        Optional<List<Lesson>> lessonsForUser = courseRepository.findAllLessonsByCourseAssignedToUserId(user.getId(), course.getId());

        assertTrue(lessonsForUser.isPresent(), "Lessons for the user should be found");
        assertEquals(2, lessonsForUser.get().size(), "Number of lessons for the user should match");
    }

    @Test
    @Transactional
    void testFindAllLessonsInCourse() {
        Course course = Course.builder()
                .name("Test Course")
                .startDate(LocalDate.now())
                .status(CourseStatus.WAIT)
                .build();

        courseRepository.save(course);

        Lesson lesson1 = Lesson.builder().course(course).name("lesson1").build();
        Lesson lesson2 = Lesson.builder().course(course).name("lesson2").build();
        lessonRepository.save(lesson1);
        lessonRepository.save(lesson2);

        Optional<List<Lesson>> lessonsInCourse = courseRepository.findAllLessonsInCourse(course.getId());

        assertTrue(lessonsInCourse.isPresent(), "Lessons in course should be found");
        assertEquals(2, lessonsInCourse.get().size(), "Number of lessons in course should match");
    }

    @Test
    @Transactional
    void testFindByStartDate() {
        Course course1 = Course.builder()
                .name("Course1")
                .startDate(LocalDate.now().minusDays(5))
                .build();

        Course course2 = Course.builder()
                .name("Course2")
                .startDate(LocalDate.now().plusDays(5))
                .build();

        courseRepository.save(course1);
        courseRepository.save(course2);

        List<Course> coursesStartingAfterToday = courseRepository.findByStartDate(course2.getStartDate());

        assertEquals(1, coursesStartingAfterToday.size(), "Number of courses starting after today should match");
        assertEquals("Course2", coursesStartingAfterToday.get(0).getName(), "Course name should match");
    }
}
