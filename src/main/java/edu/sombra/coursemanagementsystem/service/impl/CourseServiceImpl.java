package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.exception.CourseAlreadyExistsException;
import edu.sombra.coursemanagementsystem.exception.CourseCreationException;
import edu.sombra.coursemanagementsystem.exception.CourseUpdateException;
import edu.sombra.coursemanagementsystem.exception.EntityDeletionException;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.service.CourseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;

    @Override
    public String create(Course course) {
        try {
            Course createdCourse = courseRepository.save(course);
            return createdCourse.getName();
        } catch (DataAccessException ex) {
            log.error("Error creating course: {}", ex.getMessage(), ex);
            throw new CourseCreationException("Failed to create course", ex);
        }
    }

    @Override
    public Course findByName(String courseName) throws EntityNotFoundException {
        return courseRepository.findByName(courseName)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with name: " + courseName));
    }

    @Override
    public Course findById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found with id: " + courseId);
                    return new EntityNotFoundException("Course not found with id: " + courseId);
                });
    }


    @Override
    public Course update(Course course) {
        Course existingCourse = findById(course.getId());
        if (!course.getName().equals(existingCourse.getName()) && (courseRepository.exist(course.getName()))) {
            throw new CourseAlreadyExistsException(course.getName());
        }
        try {
            return courseRepository.update(course);
        } catch (DataAccessException ex) {
            log.error("Error updating course with id: {}", course.getId(), ex);
            throw new CourseUpdateException("Failed to update course", ex);
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            Course course = findById(id);
            courseRepository.delete(course);
            return true;
        } catch (DataAccessException ex) {
            log.error("Error deleting course with id: {}", id, ex);
            throw new EntityDeletionException("Failed to delete course", ex);
        }
    }

    @Override
    public List<Course> findAllCourses() {
        return courseRepository.findAll();
    }
}
