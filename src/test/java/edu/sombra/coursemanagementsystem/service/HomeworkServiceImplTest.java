package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.exception.UserNotAssignedToCourseException;
import edu.sombra.coursemanagementsystem.mapper.HomeworkMapper;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.impl.HomeworkServiceImpl;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class HomeworkServiceImplTest {
    @InjectMocks
    private HomeworkServiceImpl homeworkService;

    @Mock
    private HomeworkRepository homeworkRepository;
    @Mock
    private CourseMarkService courseMarkService;
    @Mock
    private LessonService lessonService;
    @Mock
    private EnrollmentService enrollmentService;
    @Mock
    private HomeworkMapper homeworkMapper;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        homeworkService = new HomeworkServiceImpl(homeworkRepository, courseMarkService, lessonService, enrollmentService, homeworkMapper, userRepository);
    }

    private static Stream<Arguments> provideTestDataForSetInvalidMark() {
        Long userId = 1L;
        Long homeworkId = 2L;

        return Stream.of(
                Arguments.of(userId, homeworkId, 110L),
                Arguments.of(userId, homeworkId, -5L)
        );
    }

    private static Stream<Arguments> provideTestDataForSetCorrectMark() {
        Long userId = 1L;
        Long homeworkId = 2L;

        return Stream.of(
                Arguments.of(userId, homeworkId, 110L),
                Arguments.of(userId, homeworkId, -5L)
        );
    }

    private static Stream<Arguments> provideTestDataForSetMarkSuccessfully() {
        Long userId = 1L;
        Long homeworkId = 2L;
        Long mark = 90L;

        List<Homework> listWithOtherMarks = new ArrayList<>();
        Homework homeworkWithOtherMarks = Homework.builder()
                .id(1L)
                .mark(95L)
                .lesson(Lesson.builder()
                        .id(1L)
                        .course(Course.builder()
                                .id(4L)
                                .build())
                        .build())
                .user(User.builder()
                        .id(1L)
                        .build())
                .build();
        listWithOtherMarks.add(homeworkWithOtherMarks);

        List<Homework> emptyList = new ArrayList<>();

        return Stream.of(
                Arguments.of(userId, homeworkId, mark, listWithOtherMarks),
                Arguments.of(userId, homeworkId, mark, emptyList)
        );
    }

    private static Stream<Arguments> provideTestDataForIsUserUploadedHomework() {
        Long fileId = 1L;
        Long studentId = 2L;

        return Stream.of(
                Arguments.of(fileId, studentId, true),
                Arguments.of(fileId, studentId, true)
        );
    }

    @Test
    void testSaveHomework() {
        Homework homework = new Homework();

        homeworkService.save(homework);

        verify(homeworkRepository, times(1)).save(homework);
    }

    @Test
    void testSaveWithNullHomework() {
        Homework homework = null;

        assertThrows(IllegalArgumentException.class, () -> homeworkService.save(homework));
        verify(homeworkRepository, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForSetInvalidMark")
    void testSetMark_InvalidMarkValue(Long userId, Long homeworkId, Long invalidMark) {
        when(enrollmentService.isUserAssignedToCourse(userId, homeworkId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> homeworkService.setMark(userId, homeworkId, invalidMark));

        verify(homeworkRepository, never()).setMark(any(), any());
        verify(courseMarkService, never()).saveTotalMark(any(), any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForSetCorrectMark")
    void testSetMark_UserNotAssignedToCourse(Long userId, Long homeworkId, Long mark) {
        when(enrollmentService.isUserAssignedToCourse(userId, homeworkId)).thenReturn(false);

        assertThrows(UserNotAssignedToCourseException.class, () -> homeworkService.setMark(userId, homeworkId, mark));
        verify(homeworkRepository, never()).setMark(any(), any());
        verify(courseMarkService, never()).saveTotalMark(any(), any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForSetMarkSuccessfully")
    void testSetMarkSuccessfully(Long userId, Long homeworkId, Long mark, List<Homework> homeworkList) {
        when(enrollmentService.isUserAssignedToCourse(userId, homeworkId)).thenReturn(true);
        when(lessonService.findLessonByHomeworkId(homeworkId)).thenReturn(Lesson.builder()
                .id(1L)
                .course(Course.builder()
                        .id(4L)
                        .build())
                .build());
        when(homeworkRepository.calculateAverageHomeworksMarkByUserId(userId, 4L)).thenReturn(90.0);
        when(homeworkRepository.findHomeworksByCourse(4L)).thenReturn(homeworkList);

        assertDoesNotThrow(() -> homeworkService.setMark(userId, homeworkId, mark));

        verify(homeworkRepository).setMark(homeworkId, mark);
        verify(courseMarkService).saveTotalMark(userId, 4L, 90.0, true);
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForIsUserUploadedHomework")
    void testIsUserUploadedThisHomeworkWhenUploaded(Long fileId, Long studentId, boolean isUploaded) {
        when(homeworkRepository.isUserUploadedHomework(fileId, studentId)).thenReturn(isUploaded);

        boolean result = homeworkService.isUserUploadedThisHomework(fileId, studentId);
        assertTrue(result);
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForIsUserUploadedHomework")
    void testIsUserUploadedThisHomeworkWhenNotUploaded(Long fileId, Long studentId) {

        when(homeworkRepository.isUserUploadedHomework(fileId, studentId)).thenReturn(false);

        boolean result = homeworkService.isUserUploadedThisHomework(fileId, studentId);

        assertFalse(result);
    }

    @Test
    void testFindHomeworkByIdWhenHomeworkExists() {
        Long homeworkId = 1L;
        Homework homework = Homework.builder()
                .id(1L)
                .build();

        GetHomeworkDTO homeworkDTO = GetHomeworkDTO.builder()
                .id(1L)
                .build();

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.ofNullable(homework));
        when(homeworkMapper.mapToDTO(homework)).thenReturn(homeworkDTO);

        GetHomeworkDTO result = homeworkService.findHomeworkById(homeworkId);

        assertNotNull(result);
    }

    @Test
    void testFindHomeworkByIdWhenHomeworkDoesNotExist() {
        Long homeworkId = 1L;

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> homeworkService.findHomeworkById(homeworkId));
    }

    @Test
    void testDeleteHomework() {
        Long homeworkId = 1L;

        Homework homework = Homework.builder()
                .id(1L)
                .mark(95L)
                .lesson(Lesson.builder()
                        .id(1L)
                        .course(Course.builder()
                                .id(4L)
                                .build())
                        .build())
                .user(User.builder()
                        .id(1L)
                        .build())
                .build();
        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        String result = homeworkService.deleteHomework(homeworkId);

        assertEquals("Homework deleted successfully!", result);

        verify(homeworkRepository, times(1)).delete(homework);
    }

    @Test
    void testDeleteHomeworkWhenHomeworkNotFound() {
        Long homeworkId = 1L;

        when(homeworkRepository.findById(homeworkId)).thenReturn(java.util.Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> homeworkService.deleteHomework(homeworkId));

        verify(homeworkRepository, times(0)).delete(any());
    }

    @Test
    void testGetAllHomeworks() {
        List<Homework> mockHomeworkList = Arrays.asList(
                new Homework(),
                new Homework()
        );

        List<GetHomeworkDTO> mockDTOList = Arrays.asList(
                mock(GetHomeworkDTO.class),
                mock(GetHomeworkDTO.class)
        );

        when(homeworkRepository.findAll()).thenReturn(mockHomeworkList);
        when(homeworkMapper.mapToDTO(mockHomeworkList)).thenReturn(mockDTOList);

        List<GetHomeworkDTO> result = homeworkService.getAllHomeworks();

        assertNotNull(result);
        assertEquals(mockDTOList, result);
    }

    @Test
    void testGetAllHomeworksByUserWhenUserExists() {
        Long userId = 1L;

        User user = User.builder()
                .id(1L)
                .email("user@email.com")
                .build();
        List<Homework> homeworkList = Arrays.asList(
                mock(Homework.class),
                mock(Homework.class)
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(homeworkRepository.findAllByUser(userId)).thenReturn(homeworkList);
        when(homeworkMapper.mapToDTO(homeworkList)).thenReturn(Arrays.asList(
                GetHomeworkDTO.builder()
                        .id(1L)
                        .build(),
                GetHomeworkDTO.builder()
                        .id(2L)
                        .build()
        ));

        List<GetHomeworkDTO> result = homeworkService.getAllHomeworksByUser(userId);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void testGetAllHomeworksByUserWhenUserNotExists() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> homeworkService.getAllHomeworksByUser(userId));
    }

    @Test
    void testFindHomeworkWhenHomeworkExists() {
        Long homeworkId = 1L;
        Homework expectedHomework = Homework.builder()
                .id(1L)
                .build();
        when(homeworkRepository.findById(homeworkId)).thenReturn(java.util.Optional.of(expectedHomework));

        Homework result = homeworkService.findHomework(homeworkId);

        assertEquals(expectedHomework, result);
    }

    @Test
    void testFindHomeworkWhenHomeworkDoesNotExist() {
        Long homeworkId = 1L;

        when(homeworkRepository.findById(homeworkId)).thenReturn(java.util.Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> homeworkService.findHomework(homeworkId));
    }
}
