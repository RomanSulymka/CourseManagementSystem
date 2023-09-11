package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Course;

import java.util.Optional;

public interface CourseRepository {
    Optional<Course> findByName(String name);
    Course save(Course course);
    Course findById(Long courseId);

    Optional<Course> updateCourse(Long courseId, String name);

    boolean deleteCourseById(Long id);
}
