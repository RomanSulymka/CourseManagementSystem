package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentApplyForCourseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentGetByNameDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentGetDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentResponseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentUpdateDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Enrollment;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.EnrollmentException;
import edu.sombra.coursemanagementsystem.exception.UserAlreadyAssignedException;
import edu.sombra.coursemanagementsystem.mapper.CourseMapper;
import edu.sombra.coursemanagementsystem.mapper.EnrollmentMapper;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.repository.EnrollmentRepository;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.impl.EnrollmentServiceImpl;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class EnrollmentServiceImplTest {

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HomeworkRepository homeworkRepository;
    @Mock
    private EnrollmentMapper enrollmentMapper;

    @BeforeEach
    void setUp() {
        enrollmentService = new EnrollmentServiceImpl(enrollmentRepository, courseMapper, courseRepository,
                userRepository, homeworkRepository, enrollmentMapper);
    }

    private static Stream<Arguments> provideTestDataForAssignInstructor() {
        Course validCourse = new Course();
        User validInstructor = User.builder()
                .role(RoleEnum.INSTRUCTOR)
                .build();
        EnrollmentDTO validEnrollmentDTO = EnrollmentDTO.builder()
                .userEmail("user@email.com")
                .courseName("Course 1")
                .build();

        Course existingCourse = new Course();
        User existingInstructor = User.builder()
                .role(RoleEnum.INSTRUCTOR)
                .build();
        EnrollmentDTO existingEnrollmentDTO = EnrollmentDTO.builder()
                .userEmail("user@email.com")
                .courseName("Course 2")
                .build();

        return Stream.of(
                Arguments.of(validEnrollmentDTO, validCourse, validInstructor),
                Arguments.of(existingEnrollmentDTO, existingCourse, existingInstructor),
                Arguments.of(validEnrollmentDTO, validCourse, validInstructor)
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForAssignInstructor")
    void testAssignInstructorSuccessfully(EnrollmentDTO enrollmentDTO, Course course, User instructor) {
        when(courseRepository.findByName(enrollmentDTO.getCourseName())).thenReturn(Optional.ofNullable(course));
        when(userRepository.findUserByEmail(enrollmentDTO.getUserEmail())).thenReturn(instructor);
        when(enrollmentRepository.isUserAssignedToCourse(course, instructor)).thenReturn(false);

        assertDoesNotThrow(() -> enrollmentService.assignInstructor(enrollmentDTO.getCourseName(), enrollmentDTO.getUserEmail()));

        verify(enrollmentRepository).save(any());
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForAssignInstructor")
    void testAssignInstructorWithUserAlreadyAssigned(EnrollmentDTO enrollmentDTO, Course course, User instructor) {
        when(courseRepository.findByName(enrollmentDTO.getCourseName())).thenReturn(Optional.ofNullable(course));
        when(userRepository.findUserByEmail(enrollmentDTO.getUserEmail())).thenReturn(instructor);
        when(enrollmentRepository.isUserAssignedToCourse(course, instructor)).thenReturn(true);

        UserAlreadyAssignedException exception = assertThrows(UserAlreadyAssignedException.class,
                () -> enrollmentService.assignInstructor(enrollmentDTO.getCourseName(), enrollmentDTO.getUserEmail()));
        assertEquals("User is already assigned to this course", exception.getMessage());

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void testAssignInstructorWithEnrollmentException() {
        EnrollmentDTO enrollmentDTO = EnrollmentDTO.builder()
                .userEmail("user@email.com")
                .courseName("Course 1")
                .build();
        when(courseRepository.findByName(enrollmentDTO.getCourseName())).thenThrow(new EnrollmentException());

        EnrollmentException exception = assertThrows(EnrollmentException.class,
                () -> enrollmentService.assignInstructor(enrollmentDTO.getCourseName(), enrollmentDTO.getUserEmail()));
        assertEquals("Failed to assign instructor", exception.getMessage());

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void testRemoveUserFromCourseAsInstructorWithOtherInstructors() {
        Long enrollmentId = 1L;

        User instructor = new User();
        instructor.setId(1L);
        instructor.setRole(RoleEnum.INSTRUCTOR);

        User instructor2 = new User();
        instructor2.setId(2L);
        instructor2.setRole(RoleEnum.INSTRUCTOR);

        Course course = Course.builder()
                .id(1L)
                .build();

        Enrollment enrollment = Enrollment.builder()
                .id(enrollmentId)
                .user(instructor)
                .course(course)
                .build();

        when(enrollmentService.findUserByEnrollment(enrollmentId)).thenReturn(instructor);
        when(enrollmentRepository.findCourseByEnrollmentId(enrollmentId)).thenReturn(course);
        List<User> instructors = new ArrayList<>();
        instructors.add(instructor);
        instructors.add(instructor2);
        when(enrollmentRepository.findAssignedInstructorsForCourse(anyLong())).thenReturn(instructors);
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.ofNullable(enrollment));

        assertDoesNotThrow(() -> enrollmentService.removeUserFromCourse(enrollmentId));

        verify(enrollmentRepository).delete(any(Enrollment.class));
    }

    @Test
    void testRemoveUserFromCourseAsStudent() {
        Long enrollmentId = 1L;

        User student = new User();
        student.setId(2L);
        student.setRole(RoleEnum.STUDENT);

        Enrollment enrollment = Enrollment.builder()
                .id(enrollmentId)
                .user(student)
                .course(mock(Course.class))
                .build();

        when(enrollmentService.findUserByEnrollment(enrollmentId)).thenReturn(student);
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.ofNullable(enrollment));

        assertDoesNotThrow(() -> enrollmentService.removeUserFromCourse(enrollmentId));

        verify(enrollmentRepository).delete(any(Enrollment.class));
    }

    @Test
    void testRemoveUserFromCourseWithInvalidEnrollmentId() {
        Long enrollmentId = 1L;
        when(enrollmentService.findUserByEnrollment(enrollmentId)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> enrollmentService.removeUserFromCourse(enrollmentId));
        assertEquals("Entity not found with ID: " + enrollmentId, exception.getMessage());

        verify(enrollmentRepository, never()).delete(any(Enrollment.class));
    }

    @Test
    void testFindUserByEnrollmentSuccessfully() {
        Long enrollmentId = 1L;
        User expectedUser = new User();

        when(enrollmentRepository.findUserByEnrollmentId(enrollmentId)).thenReturn(expectedUser);

        User resultUser = assertDoesNotThrow(() -> enrollmentService.findUserByEnrollment(enrollmentId));

        assertEquals(expectedUser, resultUser);
    }

    @Test
    void testFindUserByEnrollmentWithNoResultException() {
        Long enrollmentId = 1L;

        when(enrollmentRepository.findUserByEnrollmentId(enrollmentId)).thenThrow(new EntityNotFoundException());

        EnrollmentException exception = assertThrows(EnrollmentException.class, () -> enrollmentService.findUserByEnrollment(enrollmentId));
        assertEquals("Entity not found with ID: " + enrollmentId, exception.getMessage());
    }

    @Test
    void testFindEnrollmentByIdSuccessfully() {
        Long enrollmentId = 1L;
        Enrollment expectedEnrollment = Enrollment.builder()
                .id(1L)
                .course(Course.builder()
                        .id(1L)
                        .name("Course 1")
                        .build())
                .user(User.builder()
                        .id(1L)
                        .email("user@email.com")
                        .role(RoleEnum.INSTRUCTOR)
                        .build())
                .build();

        EnrollmentGetDTO enrollmentGetDTO = EnrollmentGetDTO.builder()
                .userEmail("user@email.com")
                .courseName("Course 1")
                .role(RoleEnum.INSTRUCTOR)
                .build();

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(expectedEnrollment));
        when(enrollmentMapper.mapToEnrollmentGetDTO(expectedEnrollment)).thenReturn(enrollmentGetDTO);

        EnrollmentGetDTO resultDTO = assertDoesNotThrow(() -> enrollmentService.findEnrolmentById(enrollmentId));

        assertNotNull(resultDTO);
        assertEquals(expectedEnrollment.getUser().getEmail(), resultDTO.getUserEmail());
        assertEquals(expectedEnrollment.getCourse().getName(), resultDTO.getCourseName());
        assertEquals(expectedEnrollment.getUser().getRole(), resultDTO.getRole());
    }

    @Test
    void testFindEnrollmentByIdWithEntityNotFoundException() {
        Long enrollmentId = 1L;

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> enrollmentService.findEnrolmentById(enrollmentId));
        assertEquals("Enrollment not found with id: " + enrollmentId, exception.getMessage());
    }

    @Test
    void testUpdateEnrollmentSuccessfully() {
        EnrollmentUpdateDTO updateDTO = new EnrollmentUpdateDTO();
        updateDTO.setUserId(1L);
        updateDTO.setCourseId(1L);
        updateDTO.setId(1L);
        Course mockCourse = mock(Course.class);
        Enrollment mockEnrollment = mock(Enrollment.class);
        EnrollmentGetByNameDTO expectedDTO = new EnrollmentGetByNameDTO("name", "firstName", "lastName", "email.com", RoleEnum.INSTRUCTOR);

        when(enrollmentRepository.findById(updateDTO.getId())).thenReturn(Optional.of(mock(Enrollment.class)));
        when(userRepository.findById(updateDTO.getUserId())).thenReturn(Optional.of(mock(User.class)));
        when(courseRepository.findById(updateDTO.getCourseId())).thenReturn(Optional.ofNullable(mockCourse));
        when(enrollmentRepository.update(any(Enrollment.class))).thenReturn(mockEnrollment);
        when(enrollmentMapper.mapToDTO(mockEnrollment)).thenReturn(expectedDTO);

        EnrollmentGetByNameDTO resultDTO = assertDoesNotThrow(() -> enrollmentService.updateEnrollment(updateDTO));

        assertNotNull(resultDTO);
    }

    @Test
    void testUpdateEnrollmentWithNullPointerException() {
        EnrollmentUpdateDTO updateDTO = new EnrollmentUpdateDTO();

        EnrollmentException exception = assertThrows(EnrollmentException.class, () -> enrollmentService.updateEnrollment(updateDTO));
        assertEquals("Elements are empty!", exception.getMessage());
    }

    @Test
    void testApplyForCourseAsStudentWithinLimit() {
        EnrollmentApplyForCourseDTO applyForCourseDTO = EnrollmentApplyForCourseDTO.builder()
                .courseName("Course1")
                .userId(2L)
                .build();

        String userEmail = "student@example.com";
        User mockUser = User.builder()
                .id(2L)
                .role(RoleEnum.STUDENT)
                .email(userEmail)
                .build();
        Course mockCourse = Course.builder()
                .id(2L)
                .name("Course 1")
                .build();
        Enrollment mockEnrollment = Enrollment.builder()
                .course(mockCourse)
                .user(mockUser)
                .build();
        Lesson mockLesson = Lesson.builder()
                .id(2L)
                .course(mockCourse)
                .build();
        EnrollmentResponseDTO enrollmentResponse = EnrollmentResponseDTO.builder()
                .courseId(mockCourse.getId())
                .userId(mockUser.getId())
                .userEmail(mockUser.getEmail())
                .courseName(mockCourse.getName())
                .role(mockUser.getRole())
                .build();

        when(userRepository.findUserByEmail(userEmail)).thenReturn(mockUser);
        when(enrollmentRepository.getUserRegisteredCourseCount(mockUser.getId())).thenReturn(3L);
        when(courseRepository.findByName(applyForCourseDTO.getCourseName())).thenReturn(Optional.ofNullable(mockCourse));
        when(courseRepository.findAllLessonsInCourse(mockCourse.getId())).thenReturn(Optional.of(List.of(mockLesson)));
        when(enrollmentMapper.mapToResponseDTO(mockEnrollment)).thenReturn(enrollmentResponse);

        EnrollmentResponseDTO responseDTO = enrollmentService.applyForCourse(applyForCourseDTO, userEmail);

        assertEquals(mockEnrollment.getCourse().getName(), responseDTO.getCourseName());
        verify(enrollmentRepository, times(1)).save(mockEnrollment);
        verify(homeworkRepository, times(1)).assignUserForLesson(mockUser.getId(), 2L);
    }

    @Test
    void testApplyForCourseAsStudentExceedingLimit() {
        EnrollmentApplyForCourseDTO applyForCourseDTO = EnrollmentApplyForCourseDTO.builder().build();
        String userEmail = "student@example.com";
        User mockUser = mock(User.class);

        when(userRepository.findUserByEmail(userEmail)).thenReturn(mockUser);
        when(mockUser.getRole()).thenReturn(RoleEnum.STUDENT);
        when(enrollmentRepository.getUserRegisteredCourseCount(mockUser.getId())).thenReturn(5L);

        EnrollmentException exception = assertThrows(EnrollmentException.class, () -> enrollmentService.applyForCourse(applyForCourseDTO, userEmail));
        assertEquals("User has already assigned for 5 courses", exception.getMessage());
    }

    @Test
    void testApplyForCourseAsAdmin() {
        String studentMail = "student@example.com";
        EnrollmentApplyForCourseDTO applyForCourseDTO = EnrollmentApplyForCourseDTO.builder()
                .courseName("Course1")
                .userId(2L)
                .build();

        User adminUser = User.builder()
                .id(1L)
                .role(RoleEnum.ADMIN)
                .email("admin@example.com")
                .build();

        User studentUser = User.builder()
                .id(1L)
                .role(RoleEnum.STUDENT)
                .email(studentMail)
                .build();

        Course mockCourse = Course.builder()
                .id(2L)
                .name("Course 1")
                .build();

        Lesson mockLesson = Lesson.builder()
                .id(2L)
                .course(mockCourse)
                .build();


        when(userRepository.findUserByEmail(studentMail)).thenReturn(adminUser);
        when(courseRepository.findByName(applyForCourseDTO.getCourseName())).thenReturn(Optional.ofNullable(mockCourse));
        when(userRepository.findById(2L)).thenReturn(Optional.ofNullable(studentUser));
        when(courseRepository.findAllLessonsInCourse(mockCourse.getId())).thenReturn(Optional.of(List.of(mockLesson)));

        assertDoesNotThrow(() -> enrollmentService.applyForCourse(applyForCourseDTO, studentMail));
    }

    @Test
    void testFindAllCoursesByUserSuccessfully() {
        Long userId = 1L;
        List<Course> expectedCourses = Arrays.asList(
                Course.builder()
                        .id(1L)
                        .name("Java Programming")
                        .status(CourseStatus.STOP)
                        .startDate(LocalDate.of(2023, 1, 1))
                        .build(),
                Course.builder()
                        .id(2L)
                        .name("Scala Programming")
                        .status(CourseStatus.STARTED)
                        .startDate(LocalDate.of(2023, 1, 2))
                        .build()
        );

        List<CourseResponseDTO> courseResponseDTOList = Arrays.asList(
                CourseResponseDTO.builder()
                        .courseId(1L)
                        .courseName("Java Programming")
                        .status(CourseStatus.STOP)
                        .startDate(LocalDate.of(2023, 1, 1))
                        .build(),
                CourseResponseDTO.builder()
                        .courseId(2L)
                        .courseName("Scala Programming")
                        .status(CourseStatus.STARTED)
                        .startDate(LocalDate.of(2023, 1, 2))
                        .build()
        );

        when(enrollmentRepository.findCoursesByUserId(userId)).thenReturn(expectedCourses);
        when(courseMapper.mapToResponsesDTO(expectedCourses)).thenReturn(courseResponseDTOList);
        List<CourseResponseDTO> resultCourses = assertDoesNotThrow(() -> enrollmentService.findAllCoursesByUser(userId));

        assertNotNull(resultCourses);
        assertEquals(expectedCourses.size(), resultCourses.size());
    }

    @Test
    void testFindAllCoursesByUserWithEntityNotFoundException() {
        Long userId = 1L;

        when(enrollmentRepository.findCoursesByUserId(userId)).thenThrow(EntityNotFoundException.class);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> enrollmentService.findAllCoursesByUser(userId));
        assertEquals("Failed to find courses for user", exception.getMessage());
    }

    @Test
    void testIsUserAssignedToCourse() {
        Long userId = 1L;
        Long homeworkId = 2L;

        Course mockCourse = Course.builder()
                .id(3L)
                .build();
        User mockUser = User.builder()
                .id(userId)
                .build();

        when(courseRepository.findCourseByHomeworkId(homeworkId)).thenReturn(Optional.of(mockCourse));
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(mockUser));
        when(enrollmentRepository.isUserAssignedToCourse(mockCourse, mockUser)).thenReturn(true);
        boolean result = enrollmentService.isUserAssignedToCourse(userId, homeworkId);

        assertTrue(result);
    }

    @Test
    void testIsUserNotAssignedToCourse() {
        Long userId = 1L;
        Long homeworkId = 2L;

        Course mockCourse = Course.builder()
                .id(3L)
                .build();
        User mockUser = User.builder()
                .id(userId)
                .build();

        when(courseRepository.findCourseByHomeworkId(homeworkId)).thenReturn(Optional.of(mockCourse));
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(mockUser));
        when(enrollmentRepository.isUserAssignedToCourse(mockCourse, mockUser)).thenReturn(false);
        boolean result = enrollmentService.isUserAssignedToCourse(userId, homeworkId);

        assertFalse(result);
    }
}
