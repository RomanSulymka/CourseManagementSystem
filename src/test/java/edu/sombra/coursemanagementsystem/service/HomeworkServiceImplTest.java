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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Test
    void testSetMark_InvalidMarkValue() {
        Long userId = 1L;
        Long homeworkId = 2L;
        Long mark = 110L;

        when(enrollmentService.isUserAssignedToCourse(userId, homeworkId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> homeworkService.setMark(userId, homeworkId, mark));
        verify(homeworkRepository, never()).setMark(any(), any());
        verify(courseMarkService, never()).saveTotalMark(any(), any(), any(), any());
    }

    @Test
    void testSetMark_UserNotAssignedToCourse() {
        Long userId = 1L;
        Long homeworkId = 2L;
        Long mark = 75L;

        when(enrollmentService.isUserAssignedToCourse(userId, homeworkId)).thenReturn(false);

        assertThrows(UserNotAssignedToCourseException.class, () -> homeworkService.setMark(userId, homeworkId, mark));
        verify(homeworkRepository, never()).setMark(any(), any());
        verify(courseMarkService, never()).saveTotalMark(any(), any(), any(), any());
    }

    @Test
    void testSetMarkSuccessfully() {
        Long userId = 1L;
        Long homeworkId = 2L;
        Long mark = 90L;

        List<Homework> list = new ArrayList<>();
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
        list.add(homework);


        when(enrollmentService.isUserAssignedToCourse(userId, homeworkId)).thenReturn(true);
        when(lessonService.findLessonByHomeworkId(homeworkId)).thenReturn(Lesson.builder()
                .id(1L)
                .course(Course.builder()
                        .id(4L)
                        .build())
                .build());
        when(homeworkRepository.calculateAverageHomeworksMarkByUserId(userId, 4L)).thenReturn(90.0);
        when(homeworkRepository.findHomeworksByCourse(4L)).thenReturn(list);
        //when(homeworkService.isAllHomeworksGraded(userId, 4L)).thenReturn(true);

        assertDoesNotThrow(() -> homeworkService.setMark(userId, homeworkId, mark));

        verify(homeworkRepository).setMark(homeworkId, mark);
        verify(courseMarkService).saveTotalMark(userId, 4L, 90.0, true);
    }
/*
    @Test
    void testIsAllHomeworksGradedWhenAllMarksSet() {
        Long userId = 1L;
        Long courseId = 2L;

        List<Homework> homeworks = new ArrayList<>();
        homeworks.add(Homework.builder()
                .mark(80L)
                .user(User.builder().id(1L).build())
                .build());
        homeworks.add(Homework.builder()
                .mark(90L)
                .user(User.builder().id(1L).build())
                .build());

        when(homeworkRepository.findHomeworksByCourse(courseId)).thenReturn(homeworks);

        boolean result = homeworkService.isAllHomeworksGraded(userId, courseId);

        assertTrue(result);
    }

    @Test
    void testIsAllHomeworksGradedWhenSomeMarksNotGraded() {
        Long userId = 1L;
        Long courseId = 2L;

        List<Homework> homeworks = new ArrayList<>();
        homeworks.add(Homework.builder()
                .mark(80L)
                .user(User.builder().id(1L).build())
                .build());
        homeworks.add(Homework.builder()
                .mark(null)
                .user(User.builder().id(1L).build())
                .build());

        when(homeworkRepository.findHomeworksByCourse(courseId)).thenReturn(homeworks);

        boolean result = homeworkService.isAllHomeworksGraded(userId, courseId);

        assertFalse(result);
    }*/

   /* @Test
    void testIsAllHomeworksGradedWhenNoHomeworks() {
        Long userId = 1L;
        Long courseId = 2L;

        when(homeworkRepository.findHomeworksByCourse(courseId)).thenReturn(List.of());

        boolean result = homeworkService.isAllHomeworksGraded(userId, courseId);

        assertTrue(result);
    }
*/
    @Test
    void testIsUserUploadedThisHomeworkWhenUploaded() {
        Long fileId = 1L;
        Long studentId = 2L;

        when(homeworkRepository.isUserUploadedHomework(fileId, studentId)).thenReturn(true);

        boolean result = homeworkService.isUserUploadedThisHomework(fileId, studentId);

        assertTrue(result);
    }

    @Test
    void testIsUserUploadedThisHomeworkWhenNotUploaded() {
        Long fileId = 1L;
        Long studentId = 2L;

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

        Homework homework = new Homework();
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
