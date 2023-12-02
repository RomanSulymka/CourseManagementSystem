package edu.sombra.coursemanagementsystem.service;

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
import edu.sombra.coursemanagementsystem.mapper.CourseMapper;
import edu.sombra.coursemanagementsystem.mapper.UserMapper;
import edu.sombra.coursemanagementsystem.repository.CourseMarkRepository;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.impl.CourseServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class CourseServiceImplTest {

    @InjectMocks
    private CourseServiceImpl courseService;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseMarkRepository courseMarkRepository;
    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LessonService lessonService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private CourseFeedbackService courseFeedbackService;

    @BeforeEach
    void setUp() {
        courseService = new CourseServiceImpl(courseRepository, courseMarkRepository, userService, userRepository,
                lessonService, userMapper, courseMapper, courseFeedbackService);
    }

    private static Stream<Arguments> lessonListProvider() {
        return Stream.of(
                Arguments.of(0),
                Arguments.of(1),
                Arguments.of(5),
                Arguments.of(10)
        );
    }

    private Lesson createSampleLesson(Long id, String name) {
        return Lesson.builder()
                .id(id)
                .name(name)
                .build();
    }

    private static List<Lesson> createLessonList(int size) {
        return Stream.iterate(1, i -> i <= size, i -> i + 1)
                .map(i -> Lesson.builder().id((long) i).name("Lesson " + i).build())
                .toList();
    }

    private CourseDTO createSampleCourseDTO() {
        return CourseDTO.builder()
                .course(Course.builder()
                        .name("Sample Course")
                        .startDate(LocalDate.now().plusDays(1))
                        .build())
                .instructorEmail("instructor@example.com")
                .numberOfLessons(5L)
                .build();
    }

    private User createSampleInstructor(Long id, String email) {
        return User.builder()
                .id(id)
                .email(email)
                .role(RoleEnum.INSTRUCTOR)
                .build();
    }

    private Course createSampleCourse() {
        return Course.builder()
                .id(1L)
                .name("Sample Course")
                .status(CourseStatus.WAIT)
                .startDate(LocalDate.now().plusDays(1))
                .started(true)
                .build();
    }

    private Course createSampleCourse(Long id, String name) {
        return Course.builder()
                .id(id)
                .name(name)
                .started(true)
                .status(CourseStatus.STARTED)
                .build();
    }

    private Course createSampleCourseWithStatusStop(Long id, String name) {
        return Course.builder()
                .id(id)
                .name(name)
                .status(CourseStatus.STOP)
                .started(true)
                .build();
    }

    private User createSampleUser(Long id, String email) {
        return User.builder()
                .id(id)
                .email(email)
                .role(RoleEnum.STUDENT)
                .build();
    }

    private UserAssignedToCourseDTO createSampleUserDTO(Long id, String email) {
        return UserAssignedToCourseDTO.builder()
                .id(id)
                .email(email)
                .role(RoleEnum.STUDENT.name())
                .build();
    }

    private Optional<Course> createSampleCourse(Long courseId) {
        return Optional.of(Course.builder()
                .id(courseId)
                .name("Sample Course")
                .started(false)
                .status(CourseStatus.WAIT)
                .build());
    }

    private List<Lesson> createSampleLessons() {
        return Arrays.asList(
                Lesson.builder().id(1L).name("Lesson 1").build(),
                Lesson.builder().id(2L).name("Lesson 2").build()
        );
    }

    private static Stream<Arguments> provideTestDataForCreateCourse() {
        Course existingCourse = Course.builder()
                .id(1L)
                .startDate(LocalDate.now())
                .build();

        User instructor = User.builder()
                .id(1L)
                .role(RoleEnum.INSTRUCTOR)
                .email("instructor@example")
                .build();

        CourseDTO validCourseDTO = CourseDTO.builder()
                .course(Course.builder()
                        .name("ValidCourseName")
                        .startDate(LocalDate.now())
                        .build())
                .numberOfLessons(5L)
                .instructorEmail("instructor@example")
                .build();

        CourseDTO existingCourseDTO = CourseDTO.builder()
                .course(existingCourse)
                .numberOfLessons(5L)
                .instructorEmail("instructor@example")
                .build();

        return Stream.of(
                Arguments.of(validCourseDTO, instructor, null, false),
                Arguments.of(existingCourseDTO, instructor, existingCourse, true)
        );
    }

    private CourseMark createSampleCourseMark() {
        return CourseMark.builder().build();
    }

    private CourseFeedback createSampleCourseFeedback() {
        return CourseFeedback.builder().build();
    }

    @ParameterizedTest
    @MethodSource("lessonListProvider")
    void testStartCoursesOnScheduleSuccess() {
        LocalDate currentDate = LocalDate.now();
        List<Course> coursesToStart = new ArrayList<>();
        Course course1 = new Course();
        Course course2 = new Course();
        coursesToStart.add(course1);
        coursesToStart.add(course2);

        when(courseRepository.findByStartDate(currentDate)).thenReturn(coursesToStart);
        when(lessonService.findAllLessonsByCourse(course1.getId())).thenReturn(createLessonList(10));
        when(lessonService.findAllLessonsByCourse(course2.getId())).thenReturn(createLessonList(5));

        courseService.startCoursesOnSchedule();

        assertTrue(course1.getStarted());
        assertTrue(course2.getStarted());
        verify(courseRepository, times(1)).saveAll(coursesToStart);
    }

    @Test
    void testStartCoursesOnScheduleNotEnoughLessons() {
        LocalDate currentDate = LocalDate.now();
        List<Course> coursesToStart = new ArrayList<>();
        Course course = new Course();
        course.setId(1L);
        course.setStarted(false);
        course.setStartDate(LocalDate.parse("2023-09-21"));
        coursesToStart.add(course);

        when(courseRepository.findByStartDate(currentDate)).thenReturn(coursesToStart);
        when(lessonService.findAllLessonsByCourse(course.getId())).thenReturn(createLessonList(3));

        CourseException exception = assertThrows(CourseException.class,
                () -> courseService.startCoursesOnSchedule());

        assertEquals("Course has not enough lessons", exception.getMessage());
        assertFalse(course.getStarted());
        verify(courseRepository, never()).saveAll(any());
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForCreateCourse")
    void testCreateCourse(CourseDTO courseDTO, User user) {

        when(courseRepository.exist(courseDTO.getCourse().getName())).thenReturn(false);
        when(courseRepository.save(any())).thenAnswer(invocation -> {
            Course savedCourse = invocation.getArgument(0);
            savedCourse.setId(1L);
            return savedCourse;
        });
        when(userRepository.findUserByEmail(courseDTO.getInstructorEmail())).thenReturn(user);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(courseDTO.getCourse()));

        Course createdCourse = assertDoesNotThrow(() -> courseService.create(courseDTO));

        assertNotNull(createdCourse);
        assertEquals(1L, createdCourse.getId());
        assertFalse(createdCourse.getStarted());
        verify(courseRepository, times(1)).save(any());
        verify(courseRepository, times(1)).assignInstructor(eq(1L), eq(1L));
        verify(lessonService, times(1)).generateAndAssignLessons(eq(courseDTO.getNumberOfLessons()), any());
    }

    @Test
    void testCreateCourseWhenUserIsStudent() {
        Course course = Course.builder()
                .id(1L)
                .startDate(LocalDate.now())
                .build();

        CourseDTO courseDTO = CourseDTO.builder()
                .course(course)
                .numberOfLessons(5L)
                .instructorEmail("student@example")
                .build();

        User user = User.builder()
                .id(1L)
                .role(RoleEnum.STUDENT)
                .email("student@example")
                .build();
        when(courseRepository.exist(courseDTO.getCourse().getName())).thenReturn(false);
        when(courseRepository.save(any())).thenAnswer(invocation -> {
            Course savedCourse = invocation.getArgument(0);
            savedCourse.setId(1L);
            return savedCourse;
        });
        when(userRepository.findUserByEmail(courseDTO.getInstructorEmail())).thenReturn(user);

        assertThrows(CourseCreationException.class, () -> courseService.create(courseDTO));
    }

    @Test
    void testCreateCourseWithExpiredStartDate() {
        CourseDTO courseDTO = createSampleCourseDTO();
        courseDTO.getCourse().setStartDate(LocalDate.now().minusDays(1));

        assertThrows(IllegalArgumentException.class, () -> courseService.create(courseDTO));
        verify(courseRepository, never()).save(any());
        verify(courseRepository, never()).assignInstructor(anyLong(), anyLong());
        verify(lessonService, never()).generateAndAssignLessons(anyLong(), any());
    }

    @Test
    void testFindCourseByName() {
        String courseName = "Sample Course";
        Course sampleCourse = createSampleCourse();
        when(courseRepository.findByName(courseName)).thenReturn(Optional.of(sampleCourse));

        Course foundCourse = assertDoesNotThrow(() -> courseService.findByName(courseName));

        assertNotNull(foundCourse);
        assertEquals(sampleCourse.getId(), foundCourse.getId());
        assertEquals(sampleCourse.getName(), foundCourse.getName());
        assertTrue(foundCourse.getStarted());
    }

    @Test
    void testFindCourseByNameNotFound() {
        String courseName = "Nonexistent Course";
        when(courseRepository.findByName(courseName)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> courseService.findByName(courseName));

        assertEquals("Course not found with name: " + courseName, exception.getMessage());
    }

    @Test
    void testFindCourseById() {
        Long courseId = 1L;
        Course sampleCourse = createSampleCourse();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(sampleCourse));

        Course foundCourse = assertDoesNotThrow(() -> courseService.findById(courseId));

        assertNotNull(foundCourse);
        assertEquals(sampleCourse.getId(), foundCourse.getId());
        assertEquals(sampleCourse.getName(), foundCourse.getName());
        assertTrue(foundCourse.getStarted());
    }

    @Test
    void testFindCourseByIdNotFound() {
        Long courseId = 2L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> courseService.findById(courseId));

        assertEquals("Course not found with id: " + courseId, exception.getMessage());
    }

    @Test
    void testUpdateCourseSuccessfully() {
        Course existingCourse = createSampleCourse();
        Course updatedCourse = createSampleCourse();
        updatedCourse.setName("Updated Course");

        when(courseRepository.exist(updatedCourse.getName())).thenReturn(false);
        when(courseRepository.update(updatedCourse)).thenReturn(updatedCourse);
        when(courseRepository.findById(updatedCourse.getId())).thenReturn(Optional.of(existingCourse));

        Course resultCourse = assertDoesNotThrow(() -> courseService.update(updatedCourse));

        assertNotNull(resultCourse);
        assertEquals(updatedCourse.getId(), resultCourse.getId());
        assertEquals(updatedCourse.getName(), resultCourse.getName());
        assertTrue(resultCourse.getStarted());

        verify(courseRepository, times(1)).exist(updatedCourse.getName());
        verify(courseRepository, times(1)).update(updatedCourse);
        verify(courseRepository, times(1)).findById(updatedCourse.getId());
    }

    @Test
    void testUpdateCourseAlreadyExists() {
        Course existingCourse = createSampleCourse();
        Course updatedCourse = createSampleCourse();
        updatedCourse.setName("Existing Course");

        when(courseRepository.exist(updatedCourse.getName())).thenReturn(true);
        when(courseRepository.findById(updatedCourse.getId())).thenReturn(Optional.of(existingCourse));

        CourseAlreadyExistsException exception = assertThrows(CourseAlreadyExistsException.class,
                () -> courseService.update(updatedCourse));

        assertEquals("Course with this name is already exist: " + updatedCourse.getName(), exception.getMessage());

        verify(courseRepository, times(1)).exist(updatedCourse.getName());
        verify(courseRepository, never()).update(updatedCourse);
        verify(courseRepository, times(1)).findById(updatedCourse.getId());
    }

    @Test
    void testDeleteCourseSuccessfully() {
        Long courseId = 1L;
        Course existingCourse = createSampleCourse();

        when(courseRepository.findById(courseId)).thenReturn(java.util.Optional.of(existingCourse));

        boolean result = assertDoesNotThrow(() -> courseService.delete(courseId));

        assertTrue(result);

        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, times(1)).delete(existingCourse);
    }

    @Test
    void testDeleteCourseNotFound() {
        Long courseId = 1L;

        when(courseRepository.findById(courseId)).thenReturn(java.util.Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> courseService.delete(courseId));

        assertEquals("Course not found with id: " + courseId, exception.getMessage());

        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, never()).delete(any());
    }

    @Test
    void testFindAllCourses() {
        Course course1 = createSampleCourse(1L, "Course 1");
        Course course2 = createSampleCourse(2L, "Course 2");
        List<Course> expectedCourses = Arrays.asList(course1, course2);

        when(courseRepository.findAll()).thenReturn(expectedCourses);

        List<Course> resultCourses = courseService.findAllCourses();

        assertNotNull(resultCourses);
        assertEquals(expectedCourses.size(), resultCourses.size());
        assertTrue(resultCourses.containsAll(expectedCourses));

        verify(courseRepository, times(1)).findAll();
    }

    @Test
    void testUpdateStatusWhenStatusIsStarted() {
        Long courseId = 1L;
        Long userId = 2L;
        CourseStatus status = CourseStatus.STARTED;

        List<User> users = Collections.singletonList(User.builder().id(userId).role(RoleEnum.INSTRUCTOR).build());

        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(createSampleCourse(courseId, "Sample Course")));
        when(courseRepository.findUsersInCourseByRole(courseId, RoleEnum.INSTRUCTOR)).thenReturn(users);

        Course resultCourse = courseService.updateStatus(courseId, status);

        assertNotNull(resultCourse);
        assertEquals(status, resultCourse.getStatus());

        verify(courseRepository, times(2)).findById(courseId);
    }

    @Test
    void testUpdateStatusWhenStatusIsNotStarted() {
        Long courseId = 1L;
        CourseStatus status = CourseStatus.WAIT;

        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(createSampleCourse()));

        Course resultCourse = courseService.updateStatus(courseId, status);

        assertNotNull(resultCourse);
        assertEquals(status, resultCourse.getStatus());

        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, times(1)).updateStatus(courseId, status);
    }

    @Test
    void testFindCourseByHomeworkId() {
        Long userId = 1L;
        Long homeworkId = 2L;

        when(courseRepository.findCourseByHomeworkId(homeworkId))
                .thenReturn(java.util.Optional.of(createSampleCourse(1L, "Sample Course")));

        Course resultCourse = courseService.findCourseByHomeworkId(userId, homeworkId);

        assertNotNull(resultCourse);

        verify(courseRepository, times(1)).findCourseByHomeworkId(homeworkId);
    }

    @Test
    void testFindCourseByHomeworkIdWhenNotFound() {
        Long userId = 1L;
        Long homeworkId = 2L;

        when(courseRepository.findCourseByHomeworkId(homeworkId)).thenReturn(java.util.Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> courseService.findCourseByHomeworkId(userId, homeworkId));

        verify(courseRepository, times(1)).findCourseByHomeworkId(homeworkId);
    }

    @Test
    void testFindCoursesByInstructorId() {
        Long instructorId = 1L;
        List<Course> expectedCourses = Collections.singletonList(createSampleCourse(1L, "Sample Course"));

        when(userService.isUserInstructor(instructorId)).thenReturn(true);
        when(courseRepository.findCoursesByUserId(instructorId)).thenReturn(java.util.Optional.of(expectedCourses));

        List<Course> resultCourses = courseService.findCoursesByInstructorId(instructorId);

        assertNotNull(resultCourses);
        assertEquals(expectedCourses, resultCourses);

        verify(userService, times(1)).isUserInstructor(instructorId);
        verify(courseRepository, times(1)).findCoursesByUserId(instructorId);
    }

    @Test
    void testFindAllLessonsByCourse() {
        Long courseId = 1L;
        Lesson sampleLesson = createSampleLesson(1L, "Sample Lesson");
        List<Lesson> expectedLessons = Collections.singletonList(sampleLesson);

        when(courseRepository.findAllLessonsInCourse(courseId))
                .thenReturn(Optional.of(expectedLessons));

        List<Lesson> resultLessons = courseService.findAllLessonsByCourse(courseId);

        assertNotNull(resultLessons);
        assertEquals(expectedLessons, resultLessons);

        verify(courseRepository, times(1)).findAllLessonsInCourse(courseId);
    }

    @Test
    void testFindAllLessonsByCourseWhenNoneFound() {
        Long courseId = 1L;

        when(courseRepository.findAllLessonsInCourse(courseId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> courseService.findAllLessonsByCourse(courseId));

        verify(courseRepository, times(1)).findAllLessonsInCourse(courseId);
    }

    @Test
    void testFindStudentsAssignedToCourseByInstructorId() {
        Long instructorId = 1L;
        Long courseId = 3L;

        when(userService.isUserInstructor(instructorId)).thenReturn(true);

        List<User> userList = Collections.singletonList(createSampleUser(2L, "student@example.com"));
        when(courseRepository.findUsersInCourse(courseId)).thenReturn(userList);

        List<UserAssignedToCourseDTO> userDTOList = Collections.singletonList(createSampleUserDTO(2L, "student@example.com"));
        when(userMapper.mapUsersToDTO(userList)).thenReturn(userDTOList);

        List<UserAssignedToCourseDTO> resultDTOList = courseService.findStudentsAssignedToCourseByInstructorId(instructorId, courseId);

        assertNotNull(resultDTOList);
        assertFalse(resultDTOList.isEmpty());
        assertEquals(userDTOList, resultDTOList);

        verify(userService, times(1)).isUserInstructor(instructorId);
        verify(courseRepository, times(1)).findUsersInCourse(courseId);
        verify(userMapper, times(1)).mapUsersToDTO(userList);
    }

    @Test
    void testStartCourseWithInstructors() {
        Long courseId = 1L;
        CourseStatus status = CourseStatus.STARTED;

        when(courseRepository.findById(courseId))
                .thenReturn(java.util.Optional.of(createSampleCourse(courseId, "Sample Course")));
        when(courseRepository.findUsersInCourseByRole(courseId, RoleEnum.INSTRUCTOR))
                .thenReturn(Collections.singletonList(createSampleInstructor(2L, "instructor@example.com")));

        Course resultCourse = courseService.startCourse(courseId, status);

        assertNotNull(resultCourse);
        assertEquals(status, resultCourse.getStatus());

        verify(courseRepository, times(2)).findById(courseId);
        verify(courseRepository, times(1)).updateStatus(courseId, status);
        verify(courseRepository, times(1)).findUsersInCourseByRole(courseId, RoleEnum.INSTRUCTOR);
    }

    @Test
    void testStartCourseWithoutInstructors() {
        Long courseId = 1L;
        CourseStatus status = CourseStatus.STARTED;

        when(courseRepository.findById(courseId))
                .thenReturn(java.util.Optional.of(createSampleCourse(courseId, "Sample Course")));
        when(courseRepository.findUsersInCourseByRole(courseId, RoleEnum.INSTRUCTOR))
                .thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class, () -> courseService.startCourse(courseId, status));

        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, never()).updateStatus(any(), any());
        verify(courseRepository, times(1)).findUsersInCourseByRole(courseId, RoleEnum.INSTRUCTOR);
    }

    @Test
    void testFindAllLessonsByCourseAssignedToUserId() {
        Long studentId = 1L;
        Long courseId = 2L;

        when(courseRepository.isUserAssignedToCourse(studentId, courseId)).thenReturn(true);
        when(courseRepository.findById(courseId)).thenReturn(createSampleCourse(courseId));
        when(courseRepository.findAllLessonsByCourseAssignedToUserId(studentId, courseId)).thenReturn(Optional.of(createSampleLessons()));
        when(courseMarkRepository.findCourseMarkByUserIdAndCourseId(studentId, courseId)).thenReturn(Optional.of(createSampleCourseMark()));
        when(courseFeedbackService.findFeedback(studentId, courseId)).thenReturn(createSampleCourseFeedback());
        when(courseMapper.toDTO(any(), any(), any(), any(), any())).thenReturn(mock(LessonsByCourseDTO.class));

        LessonsByCourseDTO resultDTO = courseService.findAllLessonsByCourseAssignedToUserId(studentId, courseId);

        assertNotNull(resultDTO);

        verify(courseRepository, times(1)).isUserAssignedToCourse(studentId, courseId);
        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, times(1)).findAllLessonsByCourseAssignedToUserId(studentId, courseId);
        verify(courseMarkRepository, times(1)).findCourseMarkByUserIdAndCourseId(studentId, courseId);
        verify(courseFeedbackService, times(1)).findFeedback(studentId, courseId);
        verify(courseMapper, times(1)).toDTO(any(), any(), any(), any(), any());
    }

    @Test
    void testFinishCourseSuccessfully() {
        Long studentId = 1L;
        Long courseId = 2L;
        User user = User.builder()
                .id(studentId)
                .role(RoleEnum.STUDENT)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .build();
        CourseMark courseMark = CourseMark.builder().user(user).course(course).passed(false).build();

        when(courseRepository.isUserAssignedToCourse(studentId, courseId)).thenReturn(true);
        when(courseMarkRepository.findCourseMarkByUserIdAndCourseId(studentId, courseId)).thenReturn(Optional.of(courseMark));
        when(courseMarkRepository.update(courseMark)).thenReturn(CourseMark.builder().passed(true).build());

        CourseMark resultCourseMark = courseService.finishCourse(studentId, courseId);

        assertNotNull(resultCourseMark);
        assertTrue(resultCourseMark.getPassed());

        verify(courseRepository, times(1)).isUserAssignedToCourse(studentId, courseId);
        verify(courseMarkRepository, times(1)).findCourseMarkByUserIdAndCourseId(studentId, courseId);
        verify(courseMarkRepository, times(1)).update(courseMark);
    }

    @Test
    void testFinishCourseEntityNotFoundException() {
        Long studentId = 1L;
        Long courseId = 2L;

        when(courseRepository.isUserAssignedToCourse(studentId, courseId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> courseService.finishCourse(studentId, courseId));

        verify(courseRepository, times(1)).isUserAssignedToCourse(studentId, courseId);
        verify(courseMarkRepository, never()).findCourseMarkByUserIdAndCourseId(any(), any());
        verify(courseMarkRepository, never()).update(any());
    }

    @Test
    void testStartCourseAction() {
        Long courseId = 1L;
        String action = "start";
        List<User> users = Collections.singletonList(User.builder().id(1L).role(RoleEnum.INSTRUCTOR).build());


        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(createSampleCourse(courseId, "Sample Course")));
        when(courseRepository.findUsersInCourseByRole(courseId, RoleEnum.INSTRUCTOR)).thenReturn(users);

        Course resultCourse = courseService.startOrStopCourse(courseId, action);

        assertNotNull(resultCourse);
        assertEquals(CourseStatus.STARTED, resultCourse.getStatus());

        verify(courseRepository, times(2)).findById(courseId);
        verify(courseRepository, times(1)).updateStatus(courseId, CourseStatus.STARTED);
    }

    @Test
    void testStopCourseAction() {
        Long courseId = 1L;
        String action = "stop";

        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(createSampleCourseWithStatusStop(courseId, "Sample Course")));

        doNothing().when(courseRepository).updateStatus(courseId, CourseStatus.STOP);
        Course resultCourse = courseService.startOrStopCourse(courseId, action);

        assertNotNull(resultCourse);
        assertEquals(CourseStatus.STOP, resultCourse.getStatus());

        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, times(1)).updateStatus(courseId, CourseStatus.STOP);
    }

    @Test
    void testInvalidAction() {
        Long courseId = 1L;
        String action = "invalidAction";

        assertThrows(IllegalArgumentException.class, () -> courseService.startOrStopCourse(courseId, action));

        verify(courseRepository, never()).findById(anyLong());
        verify(courseRepository, never()).updateStatus(anyLong(), any());
    }
}
