package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.entity.Course;

import java.util.List;
import java.util.Optional;

public interface CourseService {
    String create(Course courseDTO);
    Course findByName(String courseName);
    Optional<Course> update(Course course);
    Course findById(Long courseId);
    boolean delete(Long id);
    List<Course> findAllCourses();
}
