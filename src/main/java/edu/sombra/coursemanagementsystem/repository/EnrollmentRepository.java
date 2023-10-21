package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.dto.enrollment.RemoveInstructorDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Enrollment;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;
import jakarta.persistence.Tuple;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends BaseRepository<Enrollment, Long> {
    void assignUserToCourse(Enrollment enrollment);

    void saveAll(Iterable<Enrollment> enrollments);

    boolean isUserAssignedToCourse(Course course, User user);

    boolean areUsersAssignedToCourse(Course course, List<User> users);

    Enrollment findByInstructorId(RemoveInstructorDTO dto);

    List<Tuple> findEnrollmentByCourseName(String name);

    Long getUserRegisteredCourseCount(Long userId);

    List<String> findCoursesByUserId(Long id);

    Course findCourseByEnrollmentId(Long id);

    List<User> findAssignedInstructorsForCourse(Long id);

    User findUserByEnrollmentId(Long id);

}
