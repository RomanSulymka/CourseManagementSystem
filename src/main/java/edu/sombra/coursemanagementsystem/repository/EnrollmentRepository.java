package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Enrollment;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;
import jakarta.persistence.Tuple;

import java.util.List;

public interface EnrollmentRepository extends BaseRepository<Enrollment, Long> {
    boolean isUserAssignedToCourse(Course course, User user);
    List<Tuple> findEnrollmentByCourseName(String name);
    Long getUserRegisteredCourseCount(Long userId);
    List<String> findCoursesByUserId(Long id);
    Course findCourseByEnrollmentId(Long id);
    List<User> findAssignedInstructorsForCourse(Long id);
    User findUserByEnrollmentId(Long id);
}
