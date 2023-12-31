package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends BaseRepository<Course, Long> {
    Optional<Course> findByName(String name);
    boolean exist(String name);
    List<User> findUsersInCourseByRole(Long id, RoleEnum roleEnum);
    Optional<List<Lesson>> findAllLessonsInCourse(Long id);
    List<Course> findByStartDate(LocalDate currentDate);
    Optional<Course> findCourseByHomeworkId(Long homeworkId);
    Optional<List<Course>> findCoursesByUserId(Long instructorId);
    List<User> findUsersInCourse(Long courseId);
    Optional<List<Lesson>> findAllLessonsByCourseAssignedToUserId(Long studentId, Long courseId);
    boolean isUserAssignedToCourse(Long userId, Long courseId);
    Optional<Course> findCourseByFileId(Long fileId);
}
