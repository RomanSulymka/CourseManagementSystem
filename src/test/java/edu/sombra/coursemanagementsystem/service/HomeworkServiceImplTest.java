package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.mapper.HomeworkMapper;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import edu.sombra.coursemanagementsystem.repository.LessonRepository;
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

import java.util.Arrays;
import java.util.Collections;
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
    private LessonRepository lessonRepository;
    @Mock
    private EnrollmentService enrollmentService;
    @Mock
    private HomeworkMapper homeworkMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        homeworkService = new HomeworkServiceImpl(homeworkRepository, courseMarkService, lessonRepository, enrollmentService, homeworkMapper, userRepository, courseRepository);
    }

    private static Stream<Arguments> provideTestDataForSetMark() {
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

        GetHomeworkDTO homeworkDTO = GetHomeworkDTO.builder()
                .id(homeworkWithOtherMarks.getId())
                .mark(homeworkWithOtherMarks.getMark())
                .userId(userId)
                .userEmail("test@email.com")
                .fileName("test.txt")
                .lesson(homeworkWithOtherMarks.getLesson())
                .build();

        return Stream.of(
                Arguments.of(userId, homeworkId, mark, homeworkWithOtherMarks, homeworkDTO)
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
    @MethodSource("provideTestDataForSetMark")
    void testSetMark_InvalidMarkValue(Long userId, Long homeworkId, Long invalidMark) {
        when(enrollmentService.isUserAssignedToCourse(userId, homeworkId)).thenReturn(true);

        assertThrows(EntityNotFoundException.class, () -> homeworkService.setMark(userId, homeworkId, invalidMark));

        verify(homeworkRepository, never()).setMark(any(), any());
        verify(courseMarkService, never()).saveTotalMark(any(), any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForSetMark")
    void testSetMark_UserNotAssignedToCourse(Long userId, Long homeworkId, Long mark) {
        when(enrollmentService.isUserAssignedToCourse(userId, homeworkId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> homeworkService.setMark(userId, homeworkId, mark));
        verify(homeworkRepository, never()).setMark(any(), any());
        verify(courseMarkService, never()).saveTotalMark(any(), any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForSetMarkSuccessfully")
    void testSetMarkSuccessfully(Long userId, Long homeworkId, Long mark, Homework homeworkWithOtherMarks, GetHomeworkDTO homeworkDTO) {
        when(enrollmentService.isUserAssignedToCourse(userId, homeworkId)).thenReturn(true);
        when(lessonRepository.findLessonByHomeworkId(homeworkId)).thenReturn(Optional.ofNullable(Lesson.builder()
                .id(1L)
                .course(Course.builder()
                        .id(4L)
                        .build())
                .build()));
        when(homeworkRepository.calculateAverageHomeworksMarkByUserId(userId, 4L)).thenReturn(90.0);
        when(homeworkRepository.findHomeworksByCourse(4L)).thenReturn(List.of(homeworkWithOtherMarks));
        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homeworkWithOtherMarks));
        when(homeworkMapper.mapToDTO(homeworkWithOtherMarks)).thenReturn(homeworkDTO);

        GetHomeworkDTO result = homeworkService.setMark(userId, homeworkId, mark);

        assertEquals(homeworkDTO.getId(), result.getId());
        assertEquals(homeworkDTO.getLesson(), result.getLesson());
        assertEquals(homeworkDTO.getMark(), result.getMark());
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
    void testFindHomeworkByIdAsAdminWhenHomeworkExists() {
        Long homeworkId = 1L;
        Homework homework = Homework.builder()
                .id(1L)
                .build();

        GetHomeworkDTO homeworkDTO = GetHomeworkDTO.builder()
                .id(1L)
                .build();
        when(userRepository.findUserByEmail("email@gmail.com")).thenReturn(User.builder().role(RoleEnum.ADMIN).build());
        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.ofNullable(homework));
        when(homeworkMapper.mapToDTO(homework)).thenReturn(homeworkDTO);

        GetHomeworkDTO result = homeworkService.findHomeworkById(homeworkId, "email@gmail.com");

        assertNotNull(result);
    }

    @Test
    void testFindHomeworkByIdAsStudentWhenHomeworkExists() {
        Long homeworkId = 1L;
        Long courseId = 1L;
        Homework homework = Homework.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .role(RoleEnum.STUDENT)
                .build();

        GetHomeworkDTO homeworkDTO = GetHomeworkDTO.builder()
                .id(1L)
                .build();
        when(userRepository.findUserByEmail("email@gmail.com")).thenReturn(user);
        when(courseRepository.findCourseByHomeworkId(homeworkId)).thenReturn(Optional.ofNullable(Course.builder().id(courseId).build()));  // Correct the courseId here
        when(courseRepository.isUserAssignedToCourse(user.getId(), courseId)).thenReturn(true);
        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.ofNullable(homework));
        when(homeworkMapper.mapToDTO(homework)).thenReturn(homeworkDTO);

        GetHomeworkDTO result = homeworkService.findHomeworkById(homeworkId, "email@gmail.com");

        assertNotNull(result);
        verify(homeworkRepository, times(1)).findById(homeworkId);
    }

    @Test
    void testFindHomeworkByIdAsInstructorWhenHomeworkExists() {
        Long homeworkId = 1L;

        when(userRepository.findUserByEmail("email@gmail.com")).thenReturn(User.builder().role(RoleEnum.INSTRUCTOR).build());
        when(courseRepository.findCourseByHomeworkId(homeworkId)).thenReturn(Optional.ofNullable(Course.builder().id(1L).build()));

        assertThrows(EntityNotFoundException.class, () -> homeworkService.findHomeworkById(homeworkId, "email@gmail.com"));
    }

    @Test
    void testFindHomeworkByIdWhenHomeworkDoesNotExist() {
        Long homeworkId = 1L;
        when(userRepository.findUserByEmail("email@gmail.com")).thenReturn(User.builder().role(RoleEnum.INSTRUCTOR).build());
        when(courseRepository.findCourseByHomeworkId(homeworkId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> homeworkService.findHomeworkById(homeworkId, "email@gmail.com"));
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
        assertDoesNotThrow(() ->homeworkService.deleteHomework(homeworkId));

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
        String userEmail = "user@email.com";
        User user = User.builder()
                .id(1L)
                .role(RoleEnum.ADMIN)
                .build();

        List<Homework> mockHomeworkList = Arrays.asList(
                new Homework(),
                new Homework()
        );

        List<GetHomeworkDTO> mockDTOList = Arrays.asList(
                mock(GetHomeworkDTO.class),
                mock(GetHomeworkDTO.class)
        );

        when(userRepository.findUserByEmail(userEmail)).thenReturn(user);
        when(homeworkRepository.findAll()).thenReturn(mockHomeworkList);
        when(homeworkMapper.mapToDTO(mockHomeworkList)).thenReturn(mockDTOList);

        List<GetHomeworkDTO> result = homeworkService.getAllHomeworks("user@email.com");

        assertNotNull(result);
        assertEquals(mockDTOList, result);
        verify(homeworkRepository, times(1)).findAll();
    }

    @Test
    void testGetAllHomeworks_WithInstructorCredentials() {
        String userEmail = "user@email.com";
        User user = User.builder()
                .id(1L)
                .role(RoleEnum.INSTRUCTOR)
                .build();

        List<Homework> mockHomeworkList = Arrays.asList(
                new Homework(),
                new Homework()
        );

        List<GetHomeworkDTO> mockDTOList = Arrays.asList(
                mock(GetHomeworkDTO.class),
                mock(GetHomeworkDTO.class)
        );

        when(userRepository.findUserByEmail(userEmail)).thenReturn(user);
        when(homeworkRepository.findAllHomeworksWithInstructorAccess(user.getId())).thenReturn(mockHomeworkList);
        when(homeworkMapper.mapToDTO(mockHomeworkList)).thenReturn(mockDTOList);

        List<GetHomeworkDTO> result = homeworkService.getAllHomeworks("user@email.com");

        assertNotNull(result);
        assertEquals(mockDTOList, result);
        verify(homeworkRepository, times(1)).findAllHomeworksWithInstructorAccess(user.getId());
    }

    @Test
    void testGetAllHomeworks_WithStudentCredentials() {
        String userEmail = "user@email.com";
        User user = User.builder()
                .id(1L)
                .role(RoleEnum.STUDENT)
                .build();

        List<Homework> mockHomeworkList = Arrays.asList(
                new Homework(),
                new Homework()
        );

        List<GetHomeworkDTO> mockDTOList = Arrays.asList(
                mock(GetHomeworkDTO.class),
                mock(GetHomeworkDTO.class)
        );

        when(userRepository.findUserByEmail(userEmail)).thenReturn(user);
        when(homeworkRepository.findAllByUser(user.getId())).thenReturn(mockHomeworkList);
        when(homeworkMapper.mapToDTO(mockHomeworkList)).thenReturn(mockDTOList);

        List<GetHomeworkDTO> result = homeworkService.getAllHomeworks("user@email.com");

        assertNotNull(result);
        assertEquals(mockDTOList, result);
        verify(homeworkRepository, times(1)).findAllByUser(user.getId());
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

    @Test
    void testFindHomeworkByUserAndLessonIdForAdmin() {
        Long userId = 1L;
        Long lessonId = 2L;
        String userEmail = "admin@example.com";
        User user = new User();
        Lesson lesson = new Lesson();
        Homework homework = new Homework();
        GetHomeworkDTO expectedDTO = new GetHomeworkDTO();

        user.setRole(RoleEnum.ADMIN);

        when(userRepository.findUserByEmail(userEmail)).thenReturn(user);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(homeworkRepository.findByUserAndLessonId(eq(userId), eq(lesson.getId())))
                .thenReturn(Optional.of(homework));
        when(homeworkMapper.mapToDTO(homework)).thenReturn(expectedDTO);

        GetHomeworkDTO resultDTO = homeworkService.findHomeworkByUserAndLessonId(userId, lessonId, userEmail);

        assertNotNull(resultDTO);
        assertEquals(expectedDTO, resultDTO);

        verify(userRepository, times(1)).findUserByEmail(userEmail);
        verify(lessonRepository, times(1)).findById(lessonId);
        verify(userRepository, times(1)).findById(userId);
        verify(homeworkMapper, times(1)).mapToDTO(homework);
    }

    @Test
    void testFindHomeworkByUserAndLessonIdForInstructor() {
        Long lessonId = 2L;
        String userEmail = "instructor@example.com";
        User user = new User();
        Lesson lesson = Lesson.builder()
                .id(1L)
                .build();

        Homework homework = Homework.builder()
                .id(1L)
                .user(user)
                .lesson(lesson)
                .build();
        GetHomeworkDTO expectedDTO = new GetHomeworkDTO();

        user.setRole(RoleEnum.INSTRUCTOR);

        when(userRepository.findUserByEmail(userEmail)).thenReturn(user);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(homeworkRepository.findAllHomeworksWithInstructorAccess(user.getId()))
                .thenReturn(Collections.singletonList(homework));
        when(homeworkMapper.mapToDTO(homework)).thenReturn(expectedDTO);

        GetHomeworkDTO resultDTO = homeworkService.findHomeworkByUserAndLessonId(null, lessonId, userEmail);

        assertNotNull(resultDTO);
        assertEquals(expectedDTO, resultDTO);

        verify(userRepository, times(1)).findUserByEmail(userEmail);
        verify(lessonRepository, times(1)).findById(lessonId);
        verify(homeworkRepository, times(1)).findAllHomeworksWithInstructorAccess(user.getId());
        verify(homeworkMapper, times(1)).mapToDTO(homework);
    }

    @Test
    void testFindHomeworkByUserAndLessonIdForStudent() {
        Long userId = null;
        Long lessonId = 2L;
        String userEmail = "student@example.com";
        User user = new User();
        Lesson lesson = new Lesson();
        Homework homework = new Homework();
        GetHomeworkDTO expectedDTO = new GetHomeworkDTO();

        user.setRole(RoleEnum.STUDENT);

        when(userRepository.findUserByEmail(userEmail)).thenReturn(user);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(homeworkRepository.findByUserAndLessonId(user.getId(), lesson.getId()))
                .thenReturn(Optional.of(homework));
        when(homeworkMapper.mapToDTO(homework)).thenReturn(expectedDTO);

        GetHomeworkDTO resultDTO = homeworkService.findHomeworkByUserAndLessonId(userId, lessonId, userEmail);

        assertNotNull(resultDTO);
        assertEquals(expectedDTO, resultDTO);

        verify(userRepository, times(1)).findUserByEmail(userEmail);
        verify(lessonRepository, times(1)).findById(lessonId);
        verify(homeworkRepository, times(1)).findByUserAndLessonId(user.getId(), lesson.getId());
        verify(homeworkMapper, times(1)).mapToDTO(homework);
    }
}
