package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.CourseAlreadyExistsException;
import edu.sombra.coursemanagementsystem.exception.CourseCreationException;
import edu.sombra.coursemanagementsystem.exception.CourseUpdateException;
import edu.sombra.coursemanagementsystem.exception.EntityDeletionException;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.service.CourseService;
import edu.sombra.coursemanagementsystem.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final UserService userService;

    @Override
    public Course create(CourseDTO courseDTO) {
        Course course = courseDTO.getCourse();
        validateCourseNotExists(course.getName());
        validateCourseStartDateNotExpired(course.getStartDate());

        Course createdCourse = saveCourse(course);
        assignInstructor(createdCourse, courseDTO.getInstructorEmail());

        return findById(createdCourse.getId());
    }

    private void validateCourseNotExists(String courseName) {
        if (courseRepository.exist(courseName)) {
            logAndThrowError("Course with name {} already exists", courseName);
        }
    }

    private void logAndThrowError(String message, Object... args) {
        log.error(message, args);
        throw new CourseCreationException(String.format(message, args));
    }

    private Course saveCourse(Course course) {
        try {
            course.setStarted(false);
            return courseRepository.save(course);
        } catch (DataAccessException ex) {
            logAndThrowError("Error creating course: {}", ex.getMessage());
        }
        return course;
    }

    private void assignInstructor(Course createdCourse, String instructorEmail) {
        User user = userService.findUserByEmail(instructorEmail);

        if (user.getRole() != RoleEnum.INSTRUCTOR) {
            logAndThrowError("User should have instructor role, but now user has role {}", user.getRole());
        }

        courseRepository.assignInstructor(createdCourse.getId(), user.getId());
        log.info("Instructor successfully assigned!");
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void startCoursesOnSchedule() {
        LocalDate currentDate = LocalDate.now();
        //FIXME: add checking if course has at least 5 lessons
        List<Course> coursesToStart = courseRepository.findByStartDate(currentDate);
        coursesToStart.forEach(course -> {
            course.setStatus(CourseStatus.STARTED);
            course.setStarted(true);
        });

        courseRepository.saveAll(coursesToStart);
        log.info("Scheduler successfully started");
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

    @Override
    public Course updateStatus(Long id, CourseStatus status) {
        if (Objects.requireNonNull(status) == CourseStatus.STARTED) {
            return startCourse(id, status);
        } else {
            return updateCourseStatus(id, status);
        }
    }

    @Override
    public List<Lesson> findAllLessonsByCourse(Long id) {
        return courseRepository.findAllLessonsInCourse(id).orElseThrow(EntityNotFoundException::new);
    }

    public Course startCourse(Long id, CourseStatus status) {
        findById(id);
        List<User> instructors = courseRepository.findUsersInCourseByRole(id, RoleEnum.INSTRUCTOR);
        if (instructors.isEmpty()) {
            log.error("Course {} should have at least 1 Instructor", id);
            throw new EntityNotFoundException("Course should have at least 1 Instructor");
        } else {
            return updateCourseStatus(id, status);
        }
    }

    private Course updateCourseStatus(Long id, CourseStatus status) {
        courseRepository.updateStatus(id, status);
        log.info("Course with id: {} changed status to {} successfully", id, status);
        return findById(id);
    }

    public boolean validateCourseStartDateNotExpired(LocalDate startDate) {
        LocalDate currentDate = LocalDate.now();

        return !currentDate.isAfter(startDate);
    }

}
