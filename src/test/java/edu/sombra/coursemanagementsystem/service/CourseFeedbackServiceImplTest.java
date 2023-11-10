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
import edu.sombra.coursemanagementsystem.service.impl.CourseFeedbackServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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


    @BeforeEach
    void setUp() {
        courseFeedbackService = new CourseFeedbackServiceImpl(userService, courseFeedbackRepository, courseRepository, courseFeedbackMapper);
    }

    //TODO: refactor
    @Test
    void testCreateFeedbackSuccess() {
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

        CourseFeedback courseFeedback = CourseFeedback.builder()
                .id(1L)
                .feedbackText("This is a great course.")
                .course(new Course())
                .student(new User())
                .instructor(instructor)
                .build();

        when(userService.findUserById(courseFeedbackDTO.getStudentId())).thenReturn(new User());
        when(courseRepository.isUserAssignedToCourse(instructor.getId(), 1L)).thenReturn(true);

        when(courseFeedbackRepository.findById(courseFeedbackDTO.getId())).thenReturn(Optional.ofNullable(courseFeedback));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(new Course()));

        when(userService.findUserByEmail("instructor@example.com")).thenReturn(instructor);
        when(courseFeedbackService.createOrUpdateFeedback(courseFeedbackDTO, instructor)).thenReturn(any());
        when(courseRepository.findById(1L)).thenReturn(Optional.of(new Course()));

        String response = courseFeedbackService.create(courseFeedbackDTO, "instructor@example.com");

        CourseFeedback savedFeedback = courseFeedbackService.findById(courseFeedback.getId());

        assertEquals(courseFeedback, savedFeedback);
        assertEquals("Feedback saved successfully", response);
    }

    @Test
    void createFeedbackWithInvalidInstructorEmail() {
        CourseFeedbackDTO courseFeedbackDTO = new CourseFeedbackDTO();
        String instructorEmail = "invalid@example.com";
        when(userService.findUserByEmail(instructorEmail)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> courseFeedbackService.create(courseFeedbackDTO, instructorEmail));
    }

    @Test
    void testCreateCourseFeedback_Failure() {
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

        assertThrows(IllegalArgumentException.class, () -> {
            courseFeedbackService.create(courseFeedbackDTO, instructor.getEmail());
        });

        verify(courseFeedbackRepository, Mockito.never()).save(Mockito.any(CourseFeedback.class));
    }

    //TODO: refactor
    @Test
    void testEdit_Success() {
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

        when(userService.findUserById(courseFeedbackDTO.getStudentId())).thenReturn(new User());
        when(courseRepository.isUserAssignedToCourse(instructor.getId(), 1L)).thenReturn(true);

        when(courseFeedbackRepository.findById(courseFeedbackDTO.getId())).thenReturn(Optional.of(feedback));

        when(courseRepository.findById(courseFeedbackDTO.getCourseId())).thenReturn(Optional.of(new Course()));

        when(userService.findUserByEmail(instructorEmail)).thenReturn(instructor);
        when(courseFeedbackService.createOrUpdateFeedback(courseFeedbackDTO, instructor)).thenReturn(any());
        when(courseFeedbackRepository.update(feedback)).thenReturn(feedback);
        when(courseRepository.findById(courseFeedbackDTO.getCourseId())).thenReturn(Optional.of(new Course()));

        GetCourseFeedbackDTO expectedDTO = GetCourseFeedbackDTO.builder()
                .id(1L)
                .instructorId(3L)
                .instructorEmail("instructor@example.com")
                .studentId(2L)
                .studentEmail("student@example.com")
                .feedbackText("This is a great course.")
                .course(new Course())
                .build();
        when(courseFeedbackMapper.mapToDTO(feedback)).thenReturn(expectedDTO);

        GetCourseFeedbackDTO resultDTO = courseFeedbackService.edit(courseFeedbackDTO, instructorEmail);

        assertEquals(expectedDTO, resultDTO);
    }

    @Test
    void testEdit_CreateOrUpdateFeedback_EntityNotFoundException() {
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

        when(userService.findUserById(courseFeedbackDTO.getStudentId())).thenReturn(new User());
        when(courseRepository.isUserAssignedToCourse(instructor.getId(), 1L)).thenReturn(true);

        when(courseFeedbackRepository.findById(courseFeedbackDTO.getId())).thenReturn(Optional.ofNullable(feedback));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(new Course()));

        when(userService.findUserByEmail(instructorEmail)).thenReturn(instructor);
        when(courseFeedbackService.createOrUpdateFeedback(courseFeedbackDTO, instructor)).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> {
            courseFeedbackService.edit(courseFeedbackDTO, instructorEmail);
        });
        verify(courseFeedbackRepository, never()).update(any());
        verify(courseFeedbackMapper, never()).mapToDTO((List<CourseFeedback>) any());
    }

    @Test
    void testFindFeedbackSuccess() {
        Long studentId = 1L;
        Long courseId = 2L;
        CourseFeedback expectedFeedback = new CourseFeedback();

        Mockito.when(courseFeedbackRepository.findFeedback(studentId, courseId))
                .thenReturn(Optional.of(expectedFeedback));

        CourseFeedback result = courseFeedbackService.findFeedback(studentId, courseId);

        Mockito.verify(courseFeedbackRepository).findFeedback(studentId, courseId);
        assertEquals(expectedFeedback, result);
    }

    @Test
    void testFindFeedbackNotFound() {
        Long studentId = 1L;
        Long courseId = 2L;

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
