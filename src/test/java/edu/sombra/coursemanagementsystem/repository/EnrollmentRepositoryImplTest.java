package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Enrollment;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
class EnrollmentRepositoryImplTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    void testIsUserAssignedToCourse() {
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

        boolean isAssigned = enrollmentRepository.isUserAssignedToCourse(course, user);

        assertFalse(isAssigned);

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .build();

        enrollmentRepository.save(enrollment);

        isAssigned = enrollmentRepository.isUserAssignedToCourse(course, user);

        assertTrue(isAssigned);
    }

    @Test
    void testFindEnrollmentByCourseName() {
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

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .build();

        enrollmentRepository.save(enrollment);

        List<Tuple> enrollments = enrollmentRepository.findEnrollmentByCourseName(course.getName());

        assertNotNull(enrollments);
    }

    @Test
    void testGetUserRegisteredCourseCount() {
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

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .build();

        enrollmentRepository.save(enrollment);

        Long courseCount = enrollmentRepository.getUserRegisteredCourseCount(user.getId());

        assertEquals(1L, courseCount);

        assertTrue(courseCount > 0);
    }

    @Test
    void testFindCoursesByUserId() {
        User user = User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("user@example.com")
                .role(RoleEnum.STUDENT)
                .build();

        userRepository.save(user);

        Course course1 = Course.builder()
                .name("Java Programming")
                .status(CourseStatus.STOP)
                .startDate(LocalDate.now())
                .build();


        Course course2 = Course.builder()
                .name("Scala Programming")
                .status(CourseStatus.STOP)
                .startDate(LocalDate.now())
                .build();

        courseRepository.saveAll(List.of(course1, course2));

        Enrollment enrollment1 = Enrollment.builder()
                .user(user)
                .course(course1)
                .build();

        Enrollment enrollment2 = Enrollment.builder()
                .user(user)
                .course(course2)
                .build();

        enrollmentRepository.saveAll(List.of(enrollment1, enrollment2));

        List<Course> courses = enrollmentRepository.findCoursesByUserId(user.getId());

        assertNotNull(courses);
        assertTrue(courses.contains(course1));
        assertTrue(courses.contains(course2));
    }

    @Test
    void testFindCourseByEnrollmentId() {
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

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .build();

        enrollmentRepository.save(enrollment);

        Course foundCourse = enrollmentRepository.findCourseByEnrollmentId(enrollment.getId());

        assertNotNull(foundCourse);
        assertEquals(course.getName(), foundCourse.getName());
    }

    @Test
    void testFindAssignedInstructorsForCourse() {
        User user = User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("user@example.com")
                .role(RoleEnum.STUDENT)
                .build();

        User instructor1 = User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("instructor1@example.com")
                .role(RoleEnum.INSTRUCTOR)
                .build();

        User instructor2 = User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("instructor2@example.com")
                .role(RoleEnum.INSTRUCTOR)
                .build();

        userRepository.saveAll(List.of(user, instructor1, instructor2));

        Course course = Course.builder()
                .name("Java Programming")
                .status(CourseStatus.STOP)
                .startDate(LocalDate.now())
                .build();

        courseRepository.save(course);

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .build();

        Enrollment enrollment2 = Enrollment.builder()
                .user(instructor1)
                .course(course)
                .build();

        Enrollment enrollment3 = Enrollment.builder()
                .user(instructor2)
                .course(course)
                .build();

        enrollmentRepository.saveAll(List.of(enrollment, enrollment2, enrollment3));

        List<User> assignedInstructors = enrollmentRepository.findAssignedInstructorsForCourse(course.getId());

        assertNotNull(assignedInstructors);
        assertTrue(assignedInstructors.contains(instructor1));
        assertTrue(assignedInstructors.contains(instructor2));
    }

    @Test
    void testFindUserByEnrollmentId() {
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

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .build();

        enrollmentRepository.save(enrollment);

        User foundUser = enrollmentRepository.findUserByEnrollmentId(enrollment.getId());

        assertNotNull(foundUser);
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

}
