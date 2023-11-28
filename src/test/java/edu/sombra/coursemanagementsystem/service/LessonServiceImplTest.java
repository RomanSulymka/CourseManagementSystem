package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.lesson.CreateLessonDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.exception.EntityDeletionException;
import edu.sombra.coursemanagementsystem.exception.LessonException;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.repository.LessonRepository;
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

import java.util.ArrayList;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lessonService = new LessonServiceImpl(lessonRepository, courseRepository);
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

    @ParameterizedTest
    @MethodSource("provideTestDataForSaveLesson")
    void testSaveLessonWithValidCourse(CreateLessonDTO lessonDTO, Course course, Lesson savedLesson) {
        when(courseRepository.findById(lessonDTO.getCourseId())).thenReturn(Optional.of(course));
        when(lessonRepository.save(any())).thenReturn(savedLesson);

        Lesson result = lessonService.save(lessonDTO);

        assertNotNull(result);
        assertEquals(savedLesson.getName(), result.getName());
        assertEquals(course, result.getCourse());
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForSaveLesson")
    void testSaveLessonWithInvalidCourse(CreateLessonDTO lessonDTO) {

        when(courseRepository.findById(lessonDTO.getCourseId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> lessonService.save(lessonDTO));
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForSaveLesson")
    void testSaveLessonSuccessfully(CreateLessonDTO lessonDTO, Course course) {
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        Lesson lesson = new Lesson();
        when(lessonRepository.save(any())).thenReturn(lesson);

        Lesson savedLesson = lessonService.save(lessonDTO);

        assertNotNull(savedLesson);
        assertEquals(lesson, savedLesson);
    }

    @Test
    void testFindLessonById_ExistingLesson_ReturnsLesson() {
        Long id = 1L;
        Lesson expectedLesson = mock(Lesson.class);
        when(lessonRepository.findById(id)).thenReturn(Optional.of(expectedLesson));

        Lesson result = lessonService.findById(id);

        assertEquals(expectedLesson, result);
    }

    @Test
    void testFindLessonById_NonExistingLesson_ThrowsEntityNotFoundException() {
        Long id = 2L;
        when(lessonRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> lessonService.findById(id));
    }

    @Test
    void testFindAllLessonsReturnsEmptyList() {
        when(lessonRepository.findAll()).thenReturn(new ArrayList<>());

        List<Lesson> lessons = lessonService.findAllLessons();

        assertNotNull(lessons);
        assertTrue(lessons.isEmpty());
    }

    @Test
    void testFindAllLessonsReturnsNonEmptyList() {
        List<Lesson> lessonList = new ArrayList<>();
        lessonList.add(mock(Lesson.class));
        lessonList.add(mock(Lesson.class));
        when(lessonRepository.findAll()).thenReturn(lessonList);

        List<Lesson> lessons = lessonService.findAllLessons();

        assertNotNull(lessons);
        assertEquals(2, lessons.size());
    }

    @Test
    void testFindAllLessonsByCourseIdSuccess() {
        List<Lesson> lessonList = new ArrayList<>();
        lessonList.add(mock(Lesson.class));
        lessonList.add(mock(Lesson.class));
        when(lessonRepository.findAllByCourseId(1L)).thenReturn(lessonList);

        List<Lesson> lessons = lessonService.findAllLessonsByCourse(1L);

        assertNotNull(lessons);
        assertEquals(2, lessons.size());
    }

    @Test
    void testFindAllLessonsByCourseIdReturnsEmptyList() {
        when(lessonRepository.findAllByCourseId(1L)).thenReturn(Collections.emptyList());

        List<Lesson> lessons = lessonService.findAllLessonsByCourse(1L);

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
        doThrow(EntityDeletionException.class).when(lessonRepository).delete(lesson);

        assertThrows(EntityDeletionException.class, () -> lessonService.deleteLesson(lessonId));
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
        Lesson originalLesson = Lesson.builder()
                .id(1L)
                .course(mock(Course.class))
                .name("Original Lesson")
                .build();
        Lesson updatedLesson = Lesson.builder()
                .id(1L)
                .course(mock(Course.class))
                .name("Updated Lesson")
                .build();

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(originalLesson));
        when(lessonRepository.update(updatedLesson)).thenReturn(updatedLesson);

        Lesson editedLesson = lessonService.editLesson(updatedLesson);

        assertNotNull(editedLesson);
        assertEquals(updatedLesson.getId(), editedLesson.getId());
        assertEquals(updatedLesson.getName(), editedLesson.getName());
        verify(lessonRepository, times(1)).findById(1L);
        verify(lessonRepository, times(1)).update(updatedLesson);
    }

    @Test
    void testEditLesson_NullInput() {
        Lesson nullLesson = null;

        assertThrows(NullPointerException.class, () -> lessonService.editLesson(nullLesson));
        verify(lessonRepository, never()).findById(anyLong());
        verify(lessonRepository, never()).update(any());
    }

    @Test
    void testEditLesson_lessonNotFound() {
        Lesson lesson = Lesson.builder()
                .id(1L)
                .course(mock(Course.class))
                .name("Original Lesson")
                .build();

        when(lessonRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> lessonService.editLesson(lesson));
        verify(lessonRepository, times(1)).findById(1L);
        verify(lessonRepository, never()).update(any());
    }
}
