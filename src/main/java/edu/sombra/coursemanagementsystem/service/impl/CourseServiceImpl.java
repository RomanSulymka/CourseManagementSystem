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
        Course course = (courseRepository.findById(courseId));
        if (course == null) {
            log.error("Course not found with id: " + courseId);
            throw new CourseNotFoundException("Course not found with id: " + courseId);
        }
        return course;
    }

    @Override
    public Optional<Course> update(Course course) {
        if (Objects.nonNull(findById(course.getId()))) {
            if (!isCourseExist(course.getName())) {
                return Optional.ofNullable(courseRepository.update(course));
            } else {
                throw new CourseAlreadyExistsException(course.getName());
            }
        } else {
            log.error("Not found course with id: " + course.getId());
            throw new CourseNotFoundException(course.getName());
        }
    }

    private boolean isCourseExist(String name) {
        return courseRepository.exist(name);
    }

    @Override
    public boolean delete(Long id) {
        if (Objects.nonNull(findById(id))) {
            return courseRepository.delete(id);
        }
        return false;
    }

    @Override
    public List<Course> findAllCourses() {
        return courseRepository.findAll();
    }
}
