package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.exception.CourseAlreadyExistsException;
import edu.sombra.coursemanagementsystem.exception.CourseNotFoundException;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;

    @Override
    public String create(Course course) {
        courseRepository.create(course);
        return course.getName();
    }

    @Override
    public Course findByName(String courseName) {
        return courseRepository.findByName(courseName)
                .orElseThrow(() -> new CourseNotFoundException(courseName));
    }

    @Override
    public Course findById(Long courseId) {
        Course course = courseRepository.findById(courseId);
        if (course == null) {
            log.error("Course not found with id: " + courseId);
            throw new CourseNotFoundException("Course not found with id: " + courseId);
        }
        return course;
    }

    @Override
    public Course update(Course course) {
        Course existingCourse = findById(course.getId());

        if (!course.getName().equals(existingCourse.getName()) && courseRepository.exist(course.getName())) {
            throw new CourseAlreadyExistsException(String.format("Course with name '%s' already exists.", course.getName()));
        }

        return courseRepository.update(course);
    }

    @Override
    public boolean delete(Long id) {
        Course course = findById(id);
        if (course != null) {
            return courseRepository.delete(id);
        }
        return false;
    }

    @Override
    public List<Course> findAllCourses() {
        return courseRepository.findAll();
    }
}
