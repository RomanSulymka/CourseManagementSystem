package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends BaseRepository<Course, Long> {
    Optional<Course> findByName(String name);

    boolean exist(String name);

    void updateStatus(Long id, CourseStatus status);

    List<User> findUsersInCourseByRole(Long id, RoleEnum roleEnum);

    Optional<List<Lesson>> findAllLessonsInCourse(Long id);

    List<Course> findByStartDate(LocalDate currentDate);

    void assignInstructor(Long courseId, Long instructorId);

    Optional<Course> findCourseByHomeworkId(Long homeworkId);
}
