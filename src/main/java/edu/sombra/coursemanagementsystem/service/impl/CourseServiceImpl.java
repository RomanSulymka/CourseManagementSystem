package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.LessonsByCourseDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserAssignedToCourseDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.CourseFeedback;
import edu.sombra.coursemanagementsystem.entity.CourseMark;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.CourseAlreadyExistsException;
import edu.sombra.coursemanagementsystem.exception.CourseCreationException;
import edu.sombra.coursemanagementsystem.exception.CourseException;
import edu.sombra.coursemanagementsystem.exception.CourseUpdateException;
import edu.sombra.coursemanagementsystem.exception.EntityDeletionException;
import edu.sombra.coursemanagementsystem.mapper.CourseMapper;
import edu.sombra.coursemanagementsystem.mapper.UserMapper;
import edu.sombra.coursemanagementsystem.repository.CourseMarkRepository;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.service.CourseFeedbackService;
import edu.sombra.coursemanagementsystem.service.CourseService;
import edu.sombra.coursemanagementsystem.service.LessonService;
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
    public static final String COURSE_START_DATE_HAS_ALREADY_EXPIRED = "Course start date has already expired!";
    public static final String USER_SHOULD_HAVE_INSTRUCTOR_ROLE_BUT_NOW_USER_HAS_ROLE = "User should have instructor role, but now user has role {}";
    public static final String INSTRUCTOR_SUCCESSFULLY_ASSIGNED = "Instructor successfully assigned!";
    public static final String SCHEDULER_SUCCESSFULLY_STARTED = "Scheduler successfully started";
    public static final String FAILED_START_COURSE_COURSE_HAS_NOT_ENOUGH_LESSONS = "Failed start course, course has not enough lessons";
    public static final String COURSE_HAS_NOT_ENOUGH_LESSONS = "Course has not enough lessons";
    public static final String COURSE_NOT_FOUND_WITH_ID = "Course not found with id: ";
    public static final String COURSE_NOT_FOUND_WITH_NAME = "Course not found with name: ";
    public static final String ERROR_UPDATING_COURSE_WITH_ID = "Error updating course with id: {}";
    public static final String FAILED_TO_UPDATE_COURSE = "Failed to update course";
    public static final String ERROR_DELETING_COURSE_WITH_ID = "Error deleting course with id: {}";
    public static final String COURSE_SHOULD_HAVE_AT_LEAST_1_INSTRUCTOR = "Course {} should have at least 1 Instructor";
    public static final String COURSE_WITH_ID_CHANGED_STATUS_TO_SUCCESSFULLY = "Course with id: {} changed status to {} successfully";
    public static final String INCORRECT_ACTION_PARAMETER = "Incorrect action parameter!";
    public static final String INSTRUCTOR_IS_NOT_ASSIGNED_TO_THIS_COURSE = "Instructor is not assigned to this course";
    private final CourseRepository courseRepository;
    private final CourseMarkRepository courseMarkRepository;
    private final UserService userService;
    private final LessonService lessonService;
    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final CourseFeedbackService courseFeedbackService;

    private static final Long MIN_LESSONS = 5L;

    @Override
    public Course create(CourseDTO courseDTO) {
        Course course = courseDTO.getCourse();
        validateCourseCreation(course.getName(), course.getStartDate());
        Course createdCourse = saveCourse(course);
        assignInstructor(createdCourse, courseDTO.getInstructorEmail());
        lessonService.generateAndAssignLessons(courseDTO.getNumberOfLessons(), createdCourse);
        return findById(createdCourse.getId());
    }

    private void validateCourseCreation(String courseName, LocalDate startDate) {
        if (courseRepository.exist(courseName)) {
            throw new CourseCreationException("Course with name " + courseName + " already exists");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(COURSE_START_DATE_HAS_ALREADY_EXPIRED);
        }
    }

    private Course saveCourse(Course course) {
        try {
            course.setStarted(false);
            return courseRepository.save(course);
        } catch (DataAccessException ex) {
            throw new CourseCreationException("Error creating course: " + ex.getMessage(), ex);
        }
    }

    private void assignInstructor(Course createdCourse, String instructorEmail) {
        User user = userService.findUserByEmail(instructorEmail);

        if (user.getRole() != RoleEnum.INSTRUCTOR) {
            log.error(USER_SHOULD_HAVE_INSTRUCTOR_ROLE_BUT_NOW_USER_HAS_ROLE, user.getRole());
            throw new CourseCreationException(String.format(USER_SHOULD_HAVE_INSTRUCTOR_ROLE_BUT_NOW_USER_HAS_ROLE, user.getRole()));
        }

        courseRepository.assignInstructor(createdCourse.getId(), user.getId());
        log.info(INSTRUCTOR_SUCCESSFULLY_ASSIGNED);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void startCoursesOnSchedule() {
        LocalDate currentDate = LocalDate.now();
        List<Course> coursesToStart = courseRepository.findByStartDate(currentDate);
        coursesToStart.forEach(course -> {
            List<Lesson> lessons = lessonService.findAllLessonsByCourse(course.getId());
            if (isCourseHasMoreLessons(lessons.size())) {
                course.setStatus(CourseStatus.STARTED);
                course.setStarted(true);
            }
        });

        courseRepository.saveAll(coursesToStart);
        log.info(SCHEDULER_SUCCESSFULLY_STARTED);
    }

    private boolean isCourseHasMoreLessons(int size) {
        if (CourseServiceImpl.MIN_LESSONS > size) {
            log.error(FAILED_START_COURSE_COURSE_HAS_NOT_ENOUGH_LESSONS);
            throw new CourseException(COURSE_HAS_NOT_ENOUGH_LESSONS);
        } else {
            return true;
        }
    }

    @Override
    public Course findByName(String courseName) throws EntityNotFoundException {
        return courseRepository.findByName(courseName)
                .orElseThrow(() -> new EntityNotFoundException(COURSE_NOT_FOUND_WITH_NAME + courseName));
    }

    @Override
    public Course findById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error(COURSE_NOT_FOUND_WITH_ID + courseId);
                    return new EntityNotFoundException(COURSE_NOT_FOUND_WITH_ID + courseId);
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
            log.error(ERROR_UPDATING_COURSE_WITH_ID, course.getId(), ex);
            throw new CourseUpdateException(FAILED_TO_UPDATE_COURSE, ex);
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            Course course = findById(id);
            courseRepository.delete(course);
            return true;
        } catch (DataAccessException ex) {
            log.error(ERROR_DELETING_COURSE_WITH_ID, id, ex);
            throw new EntityDeletionException(ERROR_DELETING_COURSE_WITH_ID, ex);
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
    public Course findCourseByHomeworkId(Long userId, Long homeworkId) {
        return courseRepository.findCourseByHomeworkId(homeworkId)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public List<Course> findCoursesByInstructorId(Long instructorId) {
        userService.isUserInstructor(instructorId);
        return findCoursesByUserId(instructorId);
    }

    @Override
    public List<Lesson> findAllLessonsByCourse(Long id) {
        return courseRepository.findAllLessonsInCourse(id).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public List<UserAssignedToCourseDTO> findStudentsAssignedToCourseByInstructorId(Long instructorId, String courseId) {
        userService.isUserInstructor(instructorId);
        //userService.isInstructorAssignedToCourse(instructorId, courseId);
        List<User> user = courseRepository.findUsersInCourse(courseId);
        return userMapper.mapUsersToDTO(user);
    }

    public Course startCourse(Long id, CourseStatus status) {
        findById(id);
        List<User> instructors = courseRepository.findUsersInCourseByRole(id, RoleEnum.INSTRUCTOR);
        if (instructors.isEmpty()) {
            log.error(COURSE_SHOULD_HAVE_AT_LEAST_1_INSTRUCTOR, id);
            throw new EntityNotFoundException(COURSE_SHOULD_HAVE_AT_LEAST_1_INSTRUCTOR);
        } else {
            return updateCourseStatus(id, status);
        }
    }

    private Course updateCourseStatus(Long id, CourseStatus status) {
        courseRepository.updateStatus(id, status);
        log.info(COURSE_WITH_ID_CHANGED_STATUS_TO_SUCCESSFULLY, id, status);
        return findById(id);
    }

    public List<Course> findCoursesByUserId(Long userId) {
        return courseRepository.findCoursesByUserId(userId)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public LessonsByCourseDTO findAllLessonsByCourseAssignedToUserId(Long studentId, Long courseId) {
        isUserAssignedToCourse(studentId, courseId);
        Course course = findById(courseId);
        CourseMark courseMark = courseMarkRepository.findCourseMarkByUserIdAndCourseId(studentId, courseId)
                .orElse(null);
        List<Lesson> lessons = courseRepository.findAllLessonsByCourseAssignedToUserId(studentId, courseId)
                .orElseThrow(EntityNotFoundException::new);
        CourseFeedback feedback = courseFeedbackService.findFeedback(studentId, courseId);
        return courseMapper.toDTO(course, lessons, courseMark, studentId, feedback);
    }

    @Override
    public CourseMark finishCourse(Long studentId, Long courseId) {
        isUserAssignedToCourse(studentId, courseId);
        CourseMark courseMark = courseMarkRepository.findCourseMarkByUserIdAndCourseId(studentId, courseId)
                .orElseThrow(EntityNotFoundException::new);
        courseMark.setPassed(true);
        return courseMarkRepository.update(courseMark);
    }

    @Override
    public Course startOrStopCourse(Long courseId, String action) {
        if (action.equals("start")) {
            return startCourse(courseId, CourseStatus.STARTED);
        } else if (action.equals("stop")) {
            return stopCourse(courseId, CourseStatus.STOP);
        } else {
            throw new IllegalArgumentException(INCORRECT_ACTION_PARAMETER);
        }
    }

    private Course stopCourse(Long courseId, CourseStatus courseStatus) {
        return updateStatus(courseId, courseStatus);
    }

    private boolean isUserAssignedToCourse(Long studentId, Long courseId) {
        boolean isAssigned = courseRepository.isUserAssignedToCourse(studentId, courseId);
        if (isAssigned) {
            return true;
        } else {
            log.error(INSTRUCTOR_IS_NOT_ASSIGNED_TO_THIS_COURSE, studentId, courseId);
            throw new EntityNotFoundException(INSTRUCTOR_IS_NOT_ASSIGNED_TO_THIS_COURSE);
        }
    }
}
