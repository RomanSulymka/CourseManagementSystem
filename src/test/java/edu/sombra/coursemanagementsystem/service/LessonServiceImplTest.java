package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.CreateLessonDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.LessonResponseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.UpdateLessonDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.LessonException;
import edu.sombra.coursemanagementsystem.mapper.CourseMapper;
import edu.sombra.coursemanagementsystem.mapper.LessonMapper;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.repository.EnrollmentRepository;
import edu.sombra.coursemanagementsystem.repository.LessonRepository;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.impl.LessonServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class LessonServiceImplTest {
    @InjectMocks
    private LessonServiceImpl lessonService;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonMapper lessonMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    EnrollmentRepository enrollmentRepository;

    @Mock
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lessonService = new LessonServiceImpl(lessonRepository, courseRepository, userRepository, enrollmentRepository, lessonMapper, courseMapper);
    }

    private static Stream<Arguments> provideTestDataForSaveLesson() {
        CreateLessonDTO lessonDTO = new CreateLessonDTO();
        lessonDTO.setCourseId(1L);
        lessonDTO.setLessonName("Test Lesson");

        Course validCourse = new Course();
        validCourse.setId(1L);

        Lesson savedLesson = Lesson.builder()
                .course(validCourse)
                .name("Test Lesson")
                .build();

        CreateLessonDTO anotherLessonDTO = new CreateLessonDTO();
        anotherLessonDTO.setCourseId(2L);
        anotherLessonDTO.setLessonName("Another Lesson");

        Course anotherCourse = new Course();
        anotherCourse.setId(2L);

        Lesson anotherSavedLesson = Lesson.builder()
                .course(anotherCourse)
                .name("Another Lesson")
                .build();

        return Stream.of(
                Arguments.of(lessonDTO, validCourse, savedLesson),
                Arguments.of(anotherLessonDTO, anotherCourse, anotherSavedLesson)
        );
    }

    private CourseResponseDTO createCourseResponseDTO() {
        return CourseResponseDTO.builder()
                .courseId(1L)
                .courseName("Sample Course")
                .status(CourseStatus.STARTED)
                .startDate(LocalDate.now().plusDays(1))
                .started(true)
                .build();
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForSaveLesson")
    void testSaveLessonWithValidCourse(CreateLessonDTO lessonDTO, Course course, Lesson mockLesson) {
        LessonResponseDTO lessonResponseDTO = LessonResponseDTO.builder()
                .id(lessonDTO.getCourseId())
                .name(lessonDTO.getLessonName())
                .course(new CourseResponseDTO())
                .build();
        when(courseRepository.findById(lessonDTO.getCourseId())).thenReturn(Optional.of(course));
        when(lessonRepository.save(any())).thenReturn(mockLesson);
        when(courseMapper.mapToResponseDTO(course)).thenReturn(new CourseResponseDTO());
        when(lessonMapper.mapToResponseDTO(mockLesson, new CourseResponseDTO())).thenReturn(lessonResponseDTO);
        LessonResponseDTO result = lessonService.save(lessonDTO);

        assertNotNull(result);
        assertEquals(mockLesson.getName(), result.getName());
        verify(lessonRepository, times(1)).save(mockLesson);
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForSaveLesson")
    void testSaveLessonWithInvalidCourse(CreateLessonDTO lessonDTO) {

        when(courseRepository.findById(lessonDTO.getCourseId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> lessonService.save(lessonDTO));
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForSaveLesson")
    void testSaveLessonSuccessfully(CreateLessonDTO lessonDTO, Course course, Lesson mockLesson) {
        LessonResponseDTO lessonResponseDTO = LessonResponseDTO.builder()
                .id(1L)
                .name("Lesson 1")
                .course(new CourseResponseDTO())
                .build();

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        when(lessonRepository.save(any())).thenReturn(mockLesson);
        when(courseMapper.mapToResponseDTO(course)).thenReturn(new CourseResponseDTO());
        when(lessonMapper.mapToResponseDTO(mockLesson, new CourseResponseDTO())).thenReturn(lessonResponseDTO);
        LessonResponseDTO savedLesson = lessonService.save(lessonDTO);

        assertNotNull(savedLesson);
        verify(lessonRepository, times(1)).save(mockLesson);
    }

    @Test
    void testFindLessonByIdAsAdmin_ExistingLesson_ReturnsLesson() {
        Long id = 1L;
        String userEmail = "user@email.com";
        User user = User.builder()
                .id(1L)
                .role(RoleEnum.ADMIN)
                .build();

        Lesson expectedLesson = mock(Lesson.class);
        LessonResponseDTO expectedResponseLessonDTO = mock(LessonResponseDTO.class);
        CourseResponseDTO expectedResponseCourseDTO = mock(CourseResponseDTO.class);

        when(userRepository.findUserByEmail(userEmail)).thenReturn(user);
        when(lessonRepository.findById(id)).thenReturn(Optional.of(expectedLesson));
        when(courseMapper.mapToResponseDTO(expectedLesson.getCourse())).thenReturn(expectedResponseCourseDTO);

        when(lessonMapper.mapToResponseDTO(expectedLesson, expectedResponseCourseDTO)).thenReturn(expectedResponseLessonDTO);

        LessonResponseDTO result = lessonService.findById(id, userEmail);

        assertEquals(expectedResponseLessonDTO, result);
        verify(lessonRepository, times(1)).findById(id);
    }

    @Test
    void testFindLessonByIdAsStudent_ExistingLesson_ReturnsLesson() {
        Long id = 1L;
        String userEmail = "user@email.com";
        User user = User.builder()
                .id(1L)
                .role(RoleEnum.STUDENT)
                .build();

        Lesson expectedLesson = mock(Lesson.class);
        LessonResponseDTO expectedResponseLessonDTO = mock(LessonResponseDTO.class);
        CourseResponseDTO expectedResponseCourseDTO = mock(CourseResponseDTO.class);

        when(userRepository.findUserByEmail(userEmail)).thenReturn(user);
        when(lessonRepository.findById(id)).thenReturn(Optional.of(expectedLesson));
        when(enrollmentRepository.isUserAssignedToCourse(expectedLesson.getCourse(), user)).thenReturn(true);
        when(courseMapper.mapToResponseDTO(expectedLesson.getCourse())).thenReturn(expectedResponseCourseDTO);

        when(lessonMapper.mapToResponseDTO(expectedLesson, expectedResponseCourseDTO)).thenReturn(expectedResponseLessonDTO);

        LessonResponseDTO result = lessonService.findById(id, userEmail);

        assertEquals(expectedResponseLessonDTO, result);
        verify(enrollmentRepository, times(1)).isUserAssignedToCourse(expectedLesson.getCourse(), user);
    }

    @Test
    void testFindLessonByIdAsInstructor_ExistingLesson_ReturnsLesson() {
        Long id = 1L;
        String userEmail = "user@email.com";
        User user = User.builder()
                .id(1L)
                .role(RoleEnum.INSTRUCTOR)
                .build();

        Lesson expectedLesson = mock(Lesson.class);

        when(userRepository.findUserByEmail(userEmail)).thenReturn(user);
        when(lessonRepository.findById(id)).thenReturn(Optional.of(expectedLesson));
        when(enrollmentRepository.isUserAssignedToCourse(expectedLesson.getCourse(), user)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> lessonService.findById(id, userEmail));
        verify(lessonRepository, times(1)).findById(id);
        verify(courseMapper, never()).mapToResponseDTO(any());
    }

    @Test
    void testFindLessonById_NonExistingLesson_ThrowsEntityNotFoundException() {
        Long id = 2L;
        when(lessonRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> lessonService.findById(id, "admin@gmail.com"));
    }

    @Test
    void testFindAllLessonsReturnsEmptyList() {
        User user = User.builder()
                .email("user@email.com")
                .role(RoleEnum.ADMIN)
                .build();

        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(user);

        when(lessonRepository.findAll()).thenReturn(new ArrayList<>());

        when(lessonMapper.mapToResponsesDTO(any(), any())).thenReturn(new ArrayList<>());

        List<LessonResponseDTO> lessons = lessonService.findAllLessons(user.getEmail());

        assertNotNull(lessons);
        assertTrue(lessons.isEmpty());

        verify(lessonRepository, times(1)).findAll();

        verify(courseMapper, never()).mapToResponseDTO(any());

        verify(lessonMapper, times(1)).mapToResponsesDTO(eq(new ArrayList<>()), eq(new ArrayList<>()));
    }

    @Test
    void testFindAllLessonsReturnsNonEmptyList() {
        User user = User.builder()
                .email("user@email.com")
                .role(RoleEnum.ADMIN)
                .build();

        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(user);

        List<Lesson> lessonList = Arrays.asList(mock(Lesson.class), mock(Lesson.class));
        List<CourseResponseDTO> courseResponseDTOList = Arrays.asList(null, null);

        when(lessonMapper.mapToResponsesDTO(eq(lessonList), eq(courseResponseDTOList)))
                .thenReturn(Arrays.asList(mock(LessonResponseDTO.class), mock(LessonResponseDTO.class)));

        when(lessonRepository.findAll()).thenReturn(lessonList);

        List<LessonResponseDTO> lessons = lessonService.findAllLessons(user.getEmail());

        assertNotNull(lessons);
        assertEquals(2, lessons.size());
    }

    @Test
    void testFindAllLessonsReturnsNonEmptyListAsInstructor() {
        User user = User.builder()
                .email("user@email.com")
                .role(RoleEnum.INSTRUCTOR)
                .build();

        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(user);

        List<Lesson> lessonList = Arrays.asList(mock(Lesson.class), mock(Lesson.class));
        List<CourseResponseDTO> courseResponseDTOList = Arrays.asList(null, null);

        when(lessonMapper.mapToResponsesDTO(eq(lessonList), eq(courseResponseDTOList)))
                .thenReturn(Arrays.asList(mock(LessonResponseDTO.class), mock(LessonResponseDTO.class)));

        when(lessonRepository.findAllLessonsByUserId(user.getId())).thenReturn(lessonList);

        List<LessonResponseDTO> lessons = lessonService.findAllLessons(user.getEmail());

        assertNotNull(lessons);
        assertEquals(2, lessons.size());
    }

    @Test
    void testFindAllLessonsByCourseIdSuccess() {
        List<Lesson> lessonList = Arrays.asList(mock(Lesson.class), mock(Lesson.class));

        when(lessonMapper.mapToResponsesDTO(
                eq(lessonList),
                anyList()
        )).thenReturn(new ArrayList<>());

        when(lessonRepository.findAllByCourseId(1L)).thenReturn(lessonList);

        List<LessonResponseDTO> lessons = lessonService.findAllLessonsByCourse(1L);

        assertNotNull(lessons);
        assertEquals(0, lessons.size());
        verify(lessonRepository, times(1)).findAllByCourseId(1L);
    }

    @Test
    void testFindAllLessonsByCourseIdReturnsEmptyList() {
        when(lessonRepository.findAllByCourseId(1L)).thenReturn(Collections.emptyList());

        List<LessonResponseDTO> lessons = lessonService.findAllLessonsByCourse(1L);

        assertNotNull(lessons);
        assertTrue(lessons.isEmpty());
    }

    @Test
    void testGenerateAndAssignLessonsWithValidInput() {
        Long numberOfLessons = 5L;
        Course course = mock(Course.class);

        List<Lesson> savedLessons = IntStream.rangeClosed(1, numberOfLessons.intValue())
                .mapToObj(i -> Lesson.builder()
                        .name("Lesson " + i)
                        .course(course)
                        .build())
                .collect(Collectors.toList());

        when(lessonRepository.saveAll(anyList())).thenReturn(savedLessons);

        List<Lesson> generatedLessons = lessonService.generateAndAssignLessons(numberOfLessons, course);

        assertEquals(numberOfLessons, generatedLessons.size());
        verify(lessonRepository).saveAll(anyList());
    }

    @Test
    void testGenerateAndAssignLessonsWithInvalidInput() {
        Long numberOfLessons = 4L;
        Course course = mock(Course.class);

        LessonException exception = assertThrows(LessonException.class,
                () -> lessonService.generateAndAssignLessons(numberOfLessons, course));

        assertEquals("Course should have at least 5 lessons", exception.getMessage());

        verify(lessonRepository, never()).saveAll(anyList());
    }

    @Test
    void testDeleteLesson_Success() {
        Long lessonId = 1L;
        Lesson lesson = new Lesson();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        lessonService.deleteLesson(lessonId);

        verify(lessonRepository, times(1)).delete(lesson);
    }


    @Test
    void testDeleteLesson_EntityDeletionException() {
        Long lessonId = 1L;
        Lesson lesson = new Lesson();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        doThrow(LessonException.class).when(lessonRepository).delete(lesson);

        assertThrows(LessonException.class, () -> lessonService.deleteLesson(lessonId));
    }

    @Test
    void testFindLessonByHomeworkId_Exists() {
        Long homeworkId = 1L;
        Lesson expectedLesson = new Lesson();
        when(lessonRepository.findLessonByHomeworkId(homeworkId)).thenReturn(Optional.of(expectedLesson));

        Lesson foundLesson = lessonService.findLessonByHomeworkId(homeworkId);

        assertEquals(expectedLesson, foundLesson);
    }

    @Test
    void testFindLessonByHomeworkId_NotFound() {
        Long homeworkId = 1L;
        when(lessonRepository.findLessonByHomeworkId(homeworkId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> lessonService.findLessonByHomeworkId(homeworkId));
    }

    @Test
    void testEditLesson_Success() {
        UpdateLessonDTO updateLessonDTO = UpdateLessonDTO.builder()
                .id(1L)
                .name("Updated Lesson")
                .courseId(1L)
                .build();

        Course course = Course.builder()
                .id(1L)
                .name("Sample Course")
                .status(CourseStatus.WAIT)
                .startDate(LocalDate.now().plusDays(1))
                .started(true)
                .build();

        Lesson updatedLesson = Lesson.builder()
                .id(updateLessonDTO.getId())
                .course(course)
                .name(updateLessonDTO.getName())
                .build();

        LessonResponseDTO lessonResponseDTO = LessonResponseDTO.builder()
                .id(updatedLesson.getId())
                .name(updatedLesson.getName())
                .course(new CourseResponseDTO())
                .build();

        CourseResponseDTO expectedResponseCourseDTO = CourseResponseDTO.builder()
                .courseId(1L)
                .courseName("Sample Course")
                .status(CourseStatus.WAIT)
                .startDate(LocalDate.now().plusDays(1))
                .started(true)
                .build();

        when(lessonRepository.findById(updateLessonDTO.getId())).thenReturn(Optional.of(updatedLesson));
        when(courseRepository.findById(updatedLesson.getCourse().getId())).thenReturn(Optional.of(course));
        when(lessonRepository.update(updatedLesson)).thenReturn(updatedLesson);
        when(courseMapper.mapToResponseDTO(updatedLesson.getCourse())).thenReturn(expectedResponseCourseDTO);
        when(lessonMapper.mapToResponseDTO(updatedLesson, expectedResponseCourseDTO)).thenReturn(lessonResponseDTO);

        LessonResponseDTO editedLesson = lessonService.editLesson(updateLessonDTO);

        assertNotNull(editedLesson);
        assertEquals(updatedLesson.getId(), editedLesson.getId());
        assertEquals(updatedLesson.getName(), editedLesson.getName());
        verify(lessonRepository, times(1)).findById(1L);
        verify(lessonRepository, times(1)).update(updatedLesson);
    }

    @Test
    void testEditLesson_NullInput() {
        UpdateLessonDTO nullLesson = null;

        assertThrows(NullPointerException.class, () -> lessonService.editLesson(nullLesson));
        verify(lessonRepository, never()).findById(anyLong());
        verify(lessonRepository, never()).update(any());
    }

    @Test
    void testEditLesson_lessonNotFound() {
        UpdateLessonDTO lesson = UpdateLessonDTO.builder()
                .id(1L)
                .courseId(1L)
                .name("Original Lesson")
                .build();

        Lesson originalLesson = Lesson.builder()
                .id(1L)
                .build();

        when(lessonRepository.findById(1L)).thenReturn(Optional.ofNullable(originalLesson));

        assertThrows(EntityNotFoundException.class, () -> lessonService.editLesson(lesson));
        verify(lessonRepository, times(1)).findById(1L);
        verify(lessonRepository, never()).update(any());
    }
}
