package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Enrollment;
import edu.sombra.coursemanagementsystem.entity.User;

import java.util.List;

public interface EnrollmentRepository {
    void addInstructorToCourse(Enrollment enrollment);
    void saveAll(Iterable<Enrollment> enrollments);
    boolean areUsersAssignedToCourse(Course course, List<User> users);
}
