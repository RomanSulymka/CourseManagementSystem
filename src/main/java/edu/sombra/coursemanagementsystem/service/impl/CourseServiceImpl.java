package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseMarkResponseDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.course.LessonsByCourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.UpdateCourseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.LessonDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.LessonResponseDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserAssignedToCourseDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.CourseFeedback;
import edu.sombra.coursemanagementsystem.entity.CourseMark;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.CourseAlreadyExistsException;
import edu.sombra.coursemanagementsystem.exception.CourseCreationException;
import edu.sombra.coursemanagementsystem.exception.CourseException;
import edu.sombra.coursemanagementsystem.mapper.CourseMapper;
import edu.sombra.coursemanagementsystem.mapper.CourseMarkMapper;
import edu.sombra.coursemanagementsystem.mapper.LessonMapper;
import edu.sombra.coursemanagementsystem.mapper.UserMapper;
import edu.sombra.coursemanagementsystem.repository.CourseFeedbackRepository;
import edu.sombra.coursemanagementsystem.repository.CourseMarkRepository;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.CourseService;
import edu.sombra.coursemanagementsystem.service.LessonService;
import edu.sombra.coursemanagementsystem.service.UserService;
import edu.sombra.coursemanagementsystem.util.BaseUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class CourseServiceImpl implements CourseService {
    public static final String COURSE_START_DATE_HAS_ALREADY_EXPIRED = "Course start date has already expired!";
    public static final String USER_SHOULD_HAVE_INSTRUCTOR_ROLE = "User should be Instructor";
    public static final String INSTRUCTOR_SUCCESSFULLY_ASSIGNED = "Instructor successfully assigned!";
    public static final String SCHEDULER_SUCCESSFULLY_STARTED = "Scheduler successfully started";
    public static final String FAILED_START_COURSE_COURSE_HAS_NOT_ENOUGH_LESSONS = "Error, course has not enough lessons";
    public static final String COURSE_HAS_NOT_ENOUGH_LESSONS = "Course has not enough lessons";
    public static final String COURSE_NOT_FOUND_WITH_ID = "Course not found with id: ";
    public static final String COURSE_NOT_FOUND_WITH_NAME = "Course not found with name: ";
    public static final String COURSE_SHOULD_HAVE_AT_LEAST_1_INSTRUCTOR = "Course {} should have at least 1 Instructor";
    public static final String COURSE_WITH_ID_CHANGED_STATUS_TO_SUCCESSFULLY = "Course with id: {} changed status to {} successfully";
    public static final String INCORRECT_ACTION_PARAMETER = "Incorrect action parameter!";
    public static final String INSTRUCTOR_IS_NOT_ASSIGNED_TO_THIS_COURSE = "Instructor is not assigned to this course";

    private final CourseRepository courseRepository;
    private final CourseFeedbackRepository courseFeedbackRepository;
    private final CourseMarkRepository courseMarkRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final HomeworkRepository homeworkRepository;
    private final LessonService lessonService;
    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final CourseMarkMapper courseMarkMapper;
    private final LessonMapper lessonMapper;

    private static final Long MIN_LESSONS = 5L;

    @Override
    public CourseResponseDTO create(CourseDTO courseDTO) {
        try {
            Course course = courseMapper.fromCourseDTO(courseDTO);
            validateCourseCreation(course.getName(), course.getStartDate());
            Course createdCourse = saveCourse(course);
            assignInstructor(createdCourse, courseDTO.getInstructorEmail());
            lessonService.generateAndAssignLessons(courseDTO.getNumberOfLessons(), createdCourse);
            return courseMapper.mapToResponseDTO(createdCourse);
        } catch (Exception e) {
            throw new CourseCreationException(e);
        }
    }

    private void validateCourseCreation(String courseName, LocalDate startDate) {
        try {
            if (courseRepository.exist(courseName)) {
                throw new IllegalArgumentException("Course with name " + courseName + " already exists");
            }
            if (startDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException(COURSE_START_DATE_HAS_ALREADY_EXPIRED);
            }
        } catch (Exception e) {
            throw new CourseCreationException(e);
        }
    }

    private Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    private void assignInstructor(Course createdCourse, String instructorEmail) {
        try {
            User user = userRepository.findUserByEmail(instructorEmail);

            if (user.getRole() != RoleEnum.INSTRUCTOR) {
                throw new CourseCreationException(USER_SHOULD_HAVE_INSTRUCTOR_ROLE);
            }

            courseRepository.assignInstructor(createdCourse.getId(), user.getId());
            log.info(INSTRUCTOR_SUCCESSFULLY_ASSIGNED);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException(USER_SHOULD_HAVE_INSTRUCTOR_ROLE);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void startCoursesOnSchedule() {
        LocalDate currentDate = LocalDate.now();
        List<Course> coursesToStart = courseRepository.findByStartDate(currentDate);
        coursesToStart.forEach(course -> {
            List<LessonResponseDTO> lessons = lessonService.findAllLessonsByCourse(course.getId());
            if (isCourseHasMoreLessons(lessons.size())) {
                course.setStatus(CourseStatus.STARTED);
            }
        });

        courseRepository.saveAll(coursesToStart);
        log.info(SCHEDULER_SUCCESSFULLY_STARTED);
    }

    private boolean isCourseHasMoreLessons(int size) {
        if (MIN_LESSONS > size) {
            log.error(FAILED_START_COURSE_COURSE_HAS_NOT_ENOUGH_LESSONS);
            throw new CourseException(COURSE_HAS_NOT_ENOUGH_LESSONS);
        } else {
            return true;
        }
    }

    @Override
    public CourseResponseDTO findByName(String courseName) throws EntityNotFoundException {
        Course course = courseRepository.findByName(courseName)
                .orElseThrow(() -> new EntityNotFoundException(COURSE_NOT_FOUND_WITH_NAME + courseName));
        return courseMapper.mapToResponseDTO(course);
    }

    @Override
    public CourseResponseDTO findById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error(COURSE_NOT_FOUND_WITH_ID + courseId);
                    return new EntityNotFoundException(COURSE_NOT_FOUND_WITH_ID + courseId);
                });
        return courseMapper.mapToResponseDTO(course);
    }


    @Override
    public CourseResponseDTO update(UpdateCourseDTO courseDTO) {
        try {
            Course existingCourse = courseRepository.findById(courseDTO.getId())
                    .orElseThrow(() -> new EntityNotFoundException(COURSE_NOT_FOUND_WITH_ID + courseDTO.getId()));
            if (!courseDTO.getName().equals(existingCourse.getName()) && (courseRepository.exist(courseDTO.getName()))) {
                throw new CourseAlreadyExistsException(courseDTO.getName());
            }
            Course courseFromDTO = courseMapper.fromDTO(courseDTO);
            BeanUtils.copyProperties(courseFromDTO, existingCourse, BaseUtil.getNullPropertyNames(courseFromDTO));
            Course updatedCourse = courseRepository.update(existingCourse);
            return courseMapper.mapToResponseDTO(updatedCourse);
        } catch (Exception e) {
            throw new EntityNotFoundException(e.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(COURSE_NOT_FOUND_WITH_ID + id);
                    return new EntityNotFoundException(COURSE_NOT_FOUND_WITH_ID + id);
                });
        courseRepository.delete(course);
    }

    @Override
    public List<CourseResponseDTO> findAllCourses() {
        List<Course> courseList = courseRepository.findAll();
        return courseMapper.mapToResponsesDTO(courseList);
    }

    @Override
    public CourseResponseDTO updateStatus(Long id, CourseStatus status) {
        if (Objects.requireNonNull(status) == CourseStatus.STARTED) {
            return startCourse(id, status);
        } else {
            return updateCourseStatus(id, status);
        }
    }

    @Override
    public CourseResponseDTO findCourseByHomeworkId(Long userId, Long homeworkId) {
        try {
            Course course = courseRepository.findCourseByHomeworkId(homeworkId)
                    .orElseThrow(EntityNotFoundException::new);
            return courseMapper.mapToResponseDTO(course);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException("Homework not found!");
        }
    }

    @Override
    public List<CourseResponseDTO> findCoursesByUserId(Long userId, String userEmail) {
        User user = getUserByEmail(userEmail);

        return switch (user.getRole()) {
            case ADMIN -> handleAdminCase(userId);
            case INSTRUCTOR -> handleInstructorCase(userId, user);
            case STUDENT -> handleStudentCase(userId, user);
        };
    }

    private User getUserByEmail(String userEmail) {
        return userRepository.findUserByEmail(userEmail);
    }

    private List<CourseResponseDTO> handleAdminCase(Long userId) {
        User user = getUserById(userId);
        List<Course> courses = getCourseListByUserId(user.getId());
        return courseMapper.mapToResponsesDTO(courses);
    }

    private List<CourseResponseDTO> handleInstructorCase(Long userId, User user) {
        if (!Objects.equals(user.getId(), userId)) {
            throw new AccessDeniedException("User hasn't access to this information!");
        }
        List<Course> courses = getCourseListByUserId(user.getId());
        userService.isUserInstructor(user.getId());
        return courseMapper.mapToResponsesDTO(courses);
    }

    private List<CourseResponseDTO> handleStudentCase(Long userId, User user) {
        if (!Objects.equals(user.getId(), userId)) {
            throw new AccessDeniedException("User hasn't access to this information!");
        }
        List<Course> courses = getCourseListByUserId(user.getId());
        return courseMapper.mapToResponsesDTO(courses);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(EntityNotFoundException::new);
    }

    private List<Course> getCourseListByUserId(Long userId) {
        return courseRepository.findCoursesByUserId(userId)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public List<LessonResponseDTO> findAllLessonsByCourse(Long id) {
        List<Lesson> lessons = courseRepository.findAllLessonsInCourse(id).orElseThrow(EntityNotFoundException::new);
        List<CourseResponseDTO> courseResponseList = lessons.stream()
                .map(lesson -> courseMapper.mapToResponseDTO(lesson.getCourse()))
                .toList();
        return lessonMapper.mapToResponsesDTO(lessons, courseResponseList);
    }

    @Override
    public List<UserAssignedToCourseDTO> findStudentsAssignedToCourseByInstructorId(Long instructorId, Long courseId) {
        userService.isUserInstructor(instructorId);
        isUserAssignedToCourse(instructorId, courseId);
        List<User> user = courseRepository.findUsersInCourse(courseId);
        return userMapper.mapUsersToDTO(user);
    }

    public CourseResponseDTO startCourse(Long courseId, CourseStatus status) {
        findById(courseId);
        List<User> instructors = courseRepository.findUsersInCourseByRole(courseId, RoleEnum.INSTRUCTOR);
        if (instructors.isEmpty()) {
            log.error(COURSE_SHOULD_HAVE_AT_LEAST_1_INSTRUCTOR, courseId);
            throw new EntityNotFoundException(COURSE_SHOULD_HAVE_AT_LEAST_1_INSTRUCTOR);
        } else {
            return updateCourseStatus(courseId, status);
        }
    }

    private CourseResponseDTO updateCourseStatus(Long courseId, CourseStatus status) {
        courseRepository.updateStatus(courseId, status);
        log.info(COURSE_WITH_ID_CHANGED_STATUS_TO_SUCCESSFULLY, courseId, status);
        return findById(courseId);
    }

    @Override
    public LessonsByCourseDTO findAllLessonsByCourseAssignedToUserId(Long studentId, Long courseId) {
        try {
            isUserAssignedToCourse(studentId, courseId);
            Course course = courseRepository.findById(courseId)
                    .orElseThrow();

            CourseMark courseMark = courseMarkRepository.findCourseMarkByUserIdAndCourseId(studentId, courseId)
                    .orElse(new CourseMark());

            List<Lesson> lessons = courseRepository.findAllLessonsByCourseAssignedToUserId(studentId, courseId)
                    .orElseThrow(EntityNotFoundException::new);

            CourseFeedback feedback = courseFeedbackRepository.findFeedback(studentId, courseId).orElse(null);

            //TODO: rewrite with batch get
            List<LessonDTO> lessonDTO = lessons.stream()
                    .map(lesson -> {
                        Optional<Homework> homeworkOptional = homeworkRepository.findByUserAndLessonId(studentId, lesson.getId());
                        return lessonMapper.toDTO(lesson, homeworkOptional.orElse(null));
                    })
                    .toList();

            return courseMapper.toDTO(course, courseMark, feedback, lessonDTO);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException("Homework not found with these parameters!");
        }
    }

    @Override
    public CourseMarkResponseDTO finishCourse(Long studentId, Long courseId) {
        isUserAssignedToCourse(studentId, courseId);
        CourseMark courseMark = courseMarkRepository.findCourseMarkByUserIdAndCourseId(studentId, courseId)
                .orElseThrow(EntityNotFoundException::new);
        courseMark.setPassed(true);
        CourseMark updatedCourseMark = courseMarkRepository.update(courseMark);
        return courseMarkMapper.toDTO(updatedCourseMark);
    }

    @Override
    public CourseResponseDTO startOrStopCourse(Long courseId, String action) {
        if (action.equals("start")) {
            List<Lesson> lessons = courseRepository.findAllLessonsInCourse(courseId).orElseThrow();
            isCourseHasMoreLessons(lessons.size());
            return startCourse(courseId, CourseStatus.STARTED);
        } else if (action.equals("stop")) {
            return stopCourse(courseId, CourseStatus.STOP);
        } else {
            throw new IllegalArgumentException(INCORRECT_ACTION_PARAMETER);
        }
    }

    private CourseResponseDTO stopCourse(Long courseId, CourseStatus courseStatus) {
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
