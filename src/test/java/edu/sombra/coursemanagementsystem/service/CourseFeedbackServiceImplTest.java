package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.feedback.CourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.dto.feedback.GetCourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.CourseFeedback;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.mapper.CourseFeedbackMapper;
import edu.sombra.coursemanagementsystem.repository.CourseFeedbackRepository;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.impl.CourseFeedbackServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class CourseFeedbackServiceImplTest {

    @InjectMocks
    private CourseFeedbackServiceImpl courseFeedbackService;

    @Mock
    private UserService userService;

    @Mock
    private CourseFeedbackRepository courseFeedbackRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseFeedbackMapper courseFeedbackMapper;

    @Mock
    private UserRepository userRepository;


    @BeforeEach
    void setUp() {
        courseFeedbackService = new CourseFeedbackServiceImpl(userService, courseFeedbackRepository, courseRepository,
                courseFeedbackMapper, userRepository);
    }

    private static Stream<Arguments> provideFeedbackTestData() {
        CourseFeedbackDTO dto1 = new CourseFeedbackDTO();
        dto1.setId(1L);
        dto1.setFeedbackText("This is a great course.");
        dto1.setCourseId(1L);
        dto1.setStudentId(2L);

        CourseFeedbackDTO dto2 = new CourseFeedbackDTO();
        dto2.setId(2L);
        dto2.setFeedbackText("This is a fantastic course.");
        dto2.setCourseId(2L);
        dto2.setStudentId(3L);

        return Stream.of(
                Arguments.of(dto1, "instructor1@example.com"),
                Arguments.of(dto2, "instructor2@example.com")
        );
    }

    private static Stream<Arguments> provideTestDataForFindFeedback() {
        return Stream.of(
                Arguments.of(1L, 2L, new CourseFeedback()),
                Arguments.of(3L, 4L, new CourseFeedback())
        );
    }

    private static Stream<Arguments> provideEditFeedbackTestData() {
        String instructorEmail = "instructor@example.com";

        CourseFeedbackDTO courseFeedbackDTO = new CourseFeedbackDTO();
        courseFeedbackDTO.setId(1L);
        courseFeedbackDTO.setFeedbackText("This is a great course.");
        courseFeedbackDTO.setCourseId(1L);
        courseFeedbackDTO.setStudentId(2L);

        User instructor = User.builder()
                .id(3L)
                .role(RoleEnum.INSTRUCTOR)
                .password("test")
                .lastName("test")
                .firstName("test")
                .email("instructor@example.com")
                .build();

        CourseFeedback feedback = CourseFeedback.builder()
                .id(1L)
                .feedbackText("This is a great course.")
                .course(new Course())
                .student(new User())
                .instructor(instructor)
                .build();

        GetCourseFeedbackDTO expectedDTO = GetCourseFeedbackDTO.builder()
                .id(1L)
                .instructorId(3L)
                .instructorEmail("instructor@example.com")
                .studentId(2L)
                .studentEmail("student@example.com")
                .feedbackText("This is a great course.")
                .course(new Course())
                .build();

        return Stream.of(
                Arguments.of(instructorEmail, courseFeedbackDTO, instructor, feedback, expectedDTO)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFeedbackTestData")
    void testCreateFeedbackSuccess(CourseFeedbackDTO courseFeedbackDTO, String instructorEmail) {
        User instructor = User.builder()
                .id(3L)
                .role(RoleEnum.INSTRUCTOR)
                .password("test")
                .lastName("test")
                .firstName("test")
                .email(instructorEmail)
                .build();

        CourseFeedback courseFeedback = CourseFeedback.builder()
                .feedbackText(courseFeedbackDTO.getFeedbackText())
                .course(new Course())
                .student(new User())
                .instructor(instructor)
                .build();

        when(courseRepository.isUserAssignedToCourse(instructor.getId(), courseFeedbackDTO.getCourseId())).thenReturn(true);

        when(courseFeedbackRepository.findById(courseFeedbackDTO.getId())).thenReturn(Optional.ofNullable(courseFeedback));

        when(courseRepository.findById(courseFeedbackDTO.getCourseId())).thenReturn(Optional.of(new Course()));
        when(courseFeedbackRepository.findFeedback(courseFeedbackDTO.getStudentId(), courseFeedbackDTO.getId())).thenReturn(Optional.ofNullable(courseFeedback));

        when(userRepository.findUserByEmail(instructorEmail)).thenReturn(instructor);
        when(userRepository.findById(courseFeedbackDTO.getStudentId())).thenReturn(Optional.of(new User()));
        when(courseFeedbackService.createOrUpdateFeedback(courseFeedbackDTO, instructor)).thenReturn(any());
        when(courseRepository.findById(courseFeedbackDTO.getCourseId())).thenReturn(Optional.of(new Course()));

        String response = courseFeedbackService.create(courseFeedbackDTO, instructorEmail);

        verify(courseFeedbackRepository, times(1)).save(any());
        assertEquals("Feedback saved successfully", response);
    }

    @Test
    void createFeedbackWithInvalidInstructorEmail() {
        CourseFeedbackDTO courseFeedbackDTO = new CourseFeedbackDTO();
        String instructorEmail = "invalid@example.com";
        //when(userService.findUserByEmail(instructorEmail)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> courseFeedbackService.create(courseFeedbackDTO, instructorEmail));
    }

    @ParameterizedTest
    @MethodSource("provideFeedbackTestData")
    void testCreateCourseFeedback_Failure(CourseFeedbackDTO courseFeedbackDTO, String instructorEmail) {

        User instructor = User.builder()
                .id(3L)
                .role(RoleEnum.INSTRUCTOR)
                .password("test")
                .lastName("test")
                .firstName("test")
                .email(instructorEmail)
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            courseFeedbackService.create(courseFeedbackDTO, instructor.getEmail());
        });

        verify(courseFeedbackRepository, Mockito.never()).save(Mockito.any(CourseFeedback.class));
    }

    @ParameterizedTest
    @MethodSource("provideEditFeedbackTestData")
    void testEdit_Success(String instructorEmail, CourseFeedbackDTO courseFeedbackDTO, User instructor, CourseFeedback feedback, GetCourseFeedbackDTO expectedDTO) {
        when(courseRepository.isUserAssignedToCourse(instructor.getId(), 1L)).thenReturn(true);

        when(courseFeedbackRepository.findById(courseFeedbackDTO.getId())).thenReturn(Optional.of(feedback));
        when(courseFeedbackRepository.findFeedback(courseFeedbackDTO.getStudentId(), courseFeedbackDTO.getId())).thenReturn(Optional.ofNullable(feedback));

        when(courseRepository.findById(courseFeedbackDTO.getCourseId())).thenReturn(Optional.of(new Course()));

        when(userRepository.findUserByEmail(instructorEmail)).thenReturn(instructor);
        when(userRepository.findById(courseFeedbackDTO.getStudentId())).thenReturn(Optional.of(new User()));
        when(courseFeedbackService.createOrUpdateFeedback(courseFeedbackDTO, instructor)).thenReturn(any());
        when(courseFeedbackRepository.update(feedback)).thenReturn(feedback);
        when(courseRepository.findById(courseFeedbackDTO.getCourseId())).thenReturn(Optional.of(new Course()));
        when(courseFeedbackMapper.mapToDTO(feedback)).thenReturn(expectedDTO);

        GetCourseFeedbackDTO resultDTO = courseFeedbackService.edit(courseFeedbackDTO, instructorEmail);

        assertEquals(expectedDTO, resultDTO);
    }

    @ParameterizedTest
    @MethodSource("provideEditFeedbackTestData")
    void testEdit_CreateOrUpdateFeedback_EntityNotFoundException(
            String instructorEmail, CourseFeedbackDTO courseFeedbackDTO, User instructor, CourseFeedback feedback) {
        when(courseRepository.isUserAssignedToCourse(instructor.getId(), 1L)).thenReturn(true);

        when(courseFeedbackRepository.findById(courseFeedbackDTO.getId())).thenReturn(Optional.ofNullable(feedback));
        when(courseFeedbackRepository.findFeedback(courseFeedbackDTO.getStudentId(), courseFeedbackDTO.getId())).thenReturn(Optional.ofNullable(feedback));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(new Course()));

        when(userRepository.findUserByEmail(instructorEmail)).thenReturn(instructor);
        when(userRepository.findById(courseFeedbackDTO.getStudentId())).thenReturn(Optional.of(new User()));
        when(courseFeedbackService.createOrUpdateFeedback(courseFeedbackDTO, instructor)).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> {
            courseFeedbackService.edit(courseFeedbackDTO, instructorEmail);
        });
        verify(courseFeedbackRepository, never()).update(any());
        verify(courseFeedbackMapper, never()).mapToDTO((List<CourseFeedback>) any());
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForFindFeedback")
    void testFindFeedbackSuccess(Long studentId, Long courseId, CourseFeedback expectedFeedback) {
        when(courseFeedbackRepository.findFeedback(studentId, courseId))
                .thenReturn(Optional.of(expectedFeedback));

        CourseFeedback result = courseFeedbackService.findFeedback(studentId, courseId);

        verify(courseFeedbackRepository).findFeedback(studentId, courseId);
        assertEquals(expectedFeedback, result);
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForFindFeedback")
    void testFindFeedbackNotFound(Long studentId, Long courseId) {

        when(courseFeedbackRepository.findFeedback(studentId, courseId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            courseFeedbackService.findFeedback(studentId, courseId);
        });
    }

    @Test
    void testFindAll() {
        List<CourseFeedback> feedbackList = new ArrayList<>();
        List<GetCourseFeedbackDTO> expectedDTOList = new ArrayList<>();

        Mockito.when(courseFeedbackRepository.findAll())
                .thenReturn(feedbackList);

        Mockito.when(courseFeedbackMapper.mapToDTO(feedbackList))
                .thenReturn(expectedDTOList);

        List<GetCourseFeedbackDTO> result = courseFeedbackService.findAll();

        Mockito.verify(courseFeedbackRepository).findAll();
        Mockito.verify(courseFeedbackMapper).mapToDTO(feedbackList);
        assertEquals(expectedDTOList, result);
    }

    @Test
    void testMapToDTO() {
        CourseFeedback feedback = new CourseFeedback();
        GetCourseFeedbackDTO expectedDTO = mock(GetCourseFeedbackDTO.class);

        Mockito.when(courseFeedbackMapper.mapToDTO(feedback))
                .thenReturn(expectedDTO);

        GetCourseFeedbackDTO result = courseFeedbackMapper.mapToDTO(feedback);

        Mockito.verify(courseFeedbackMapper).mapToDTO(feedback);
        assertEquals(expectedDTO, result);
    }

    @Test
    void testFindCourseFeedbackById() {
        Long feedbackId = 1L;
        CourseFeedback feedback = new CourseFeedback();
        GetCourseFeedbackDTO expectedDTO = mock(GetCourseFeedbackDTO.class);

        Mockito.when(courseFeedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedback));

        Mockito.when(courseFeedbackMapper.mapToDTO(feedback)).thenReturn(expectedDTO);

        GetCourseFeedbackDTO resultDTO = courseFeedbackService.findCourseFeedbackById(feedbackId);

        assertEquals(expectedDTO, resultDTO);
    }

    @Test
    void testFindCourseFeedbackById_NegativeId() {
        Long negativeId = -1L;

        when(courseFeedbackRepository.findById(negativeId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            courseFeedbackService.findCourseFeedbackById(negativeId);
        });
    }

    @Test
    void testDelete_Success() {
        Long feedbackId = 1L;
        CourseFeedback feedback = new CourseFeedback();

        when(courseFeedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedback));

        String result = courseFeedbackService.delete(feedbackId);

        assertEquals("Course Feedback deleted successfully", result);
        verify(courseFeedbackRepository, times(1)).delete(feedback);
    }

    @Test
    void testDelete_EntityNotFoundException() {
        Long feedbackId = 1L;

        when(courseFeedbackRepository.findById(feedbackId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            courseFeedbackService.delete(feedbackId);
        });
        verify(courseFeedbackRepository, never()).delete(any());
    }
}
