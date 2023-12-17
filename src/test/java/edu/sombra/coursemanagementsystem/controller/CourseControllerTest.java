package edu.sombra.coursemanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sombra.coursemanagementsystem.dto.course.CourseActionDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseAssignedToUserDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.course.LessonsByCourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.UpdateCourseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.LessonResponseDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserAssignedToCourseDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import edu.sombra.coursemanagementsystem.mapper.CourseMapper;
import edu.sombra.coursemanagementsystem.service.CourseService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

    @MockBean
    private CourseMapper courseMapper;

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testCreateCourseSuccess() throws Exception {
        CourseDTO courseDTO = CourseDTO.builder()
                .name("Java learn")
                .startDate(LocalDate.now())
                .status(CourseStatus.WAIT)
                .instructorEmail("instructor@example.com")
                .numberOfLessons(10L)
                .build();

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/course/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseDTO)));

        result.andExpect(status().isOk());

        verify(courseService, times(1)).create(courseDTO);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testEditCourseSuccess() throws Exception {
        UpdateCourseDTO course = UpdateCourseDTO.builder()
                .id(1L)
                .name("Java Programming")
                .status(CourseStatus.STOP)
                .startDate(LocalDate.of(2023, 1, 1))
                .build();

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/course/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(course)));

        result.andExpect(status().isOk());

        verify(courseService, times(1)).update(course);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindCourseByIdSuccess() throws Exception {
        Long courseId = 1L;

        CourseResponseDTO course = CourseResponseDTO.builder()
                .courseId(1L)
                .courseName("Java Programming")
                .status(CourseStatus.STOP)
                .startDate(LocalDate.of(2023, 1, 1))
                .build();

        List<Course> courses = List.of(Course.builder().id(1L).build());

        when(courseMapper.mapToResponsesDTO(courses)).thenReturn(List.of(course));
        when(courseService.findById(courseId)).thenReturn(course);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/course/{id}", courseId));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.courseId").value(courseId))
                .andExpect(jsonPath("$.courseName").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.startDate").exists());

        verify(courseService, times(1)).findById(courseId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindAllCoursesSuccess() throws Exception {
        List<CourseResponseDTO> courseResponseList = Arrays.asList(
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

        List<Course> courses = Arrays.asList(
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

        when(courseMapper.mapToResponsesDTO(courses)).thenReturn(courseResponseList);
        when(courseService.findAllCourses()).thenReturn(courseResponseList);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/course/find-all")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].courseId").exists())
                .andExpect(jsonPath("$[0].courseName").exists())
                .andExpect(jsonPath("$[0].status").exists())
                .andExpect(jsonPath("$[1].startDate").exists());

        verify(courseService, times(1)).findAllCourses();
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testDeleteCourseByIdSuccess() throws Exception {
        var courseId = 1L;

        when(courseService.delete(courseId)).thenReturn(true);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/course/{id}", courseId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());

        verify(courseService, times(1)).delete(courseId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testStartOrStopCourseSuccess() throws Exception {
        CourseResponseDTO course = CourseResponseDTO.builder()
                .courseId(1L)
                .courseName("Java Programming")
                .status(CourseStatus.STARTED)
                .startDate(LocalDate.of(2023, 1, 1))
                .build();

        CourseActionDTO courseActionDTO = CourseActionDTO.builder()
                .courseId(1L)
                .action("start")
                .build();

        when(courseService.startOrStopCourse(courseActionDTO.getCourseId(), courseActionDTO.getAction())).thenReturn(course);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseActionDTO)));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.courseId").value(courseActionDTO.getCourseId()))
                .andExpect(jsonPath("$.status").value("STARTED"))
                .andExpect(jsonPath("$.courseName").value("Java Programming"));

        verify(courseService, times(1)).startOrStopCourse(courseActionDTO.getCourseId(), courseActionDTO.getAction());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindAllLessonsByCourseSuccess() throws Exception {
        Long courseId = 1L;
        LessonResponseDTO lesson1 = LessonResponseDTO.builder().id(1L).name("Lesson 1").course(CourseResponseDTO.builder().courseId(courseId).build()).build();
        LessonResponseDTO lesson2 = LessonResponseDTO.builder().id(2L).name("Lesson 2").course(CourseResponseDTO.builder().courseId(courseId).build()).build();
        List<LessonResponseDTO> lessons = Arrays.asList(lesson1, lesson2);

        when(courseService.findAllLessonsByCourse(courseId)).thenReturn(lessons);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/course/find-all-lessons/{id}", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lessons)));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Lesson 1"))
                .andExpect(jsonPath("$[1].name").value("Lesson 2"));

        verify(courseService, times(1)).findAllLessonsByCourse(courseId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindCoursesByUserIdSuccess() throws Exception {
        Long userId = 1L;
        given(courseService.findCoursesByUserId(userId, "admin@gmail.com")).willReturn(Collections.singletonList(
                CourseResponseDTO.builder()
                        .courseId(1L)
                        .courseName("Java Programming")
                        .status(CourseStatus.FINISHED)
                        .build()
        ));

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/course/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].courseId").value(1))
                .andExpect(jsonPath("$[0].courseName").value("Java Programming"))
                .andExpect(jsonPath("$[0].status").value("FINISHED"));

        verify(courseService, times(1)).findCoursesByUserId(userId, "admin@gmail.com");
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindUsersAssignedToCourseByInstructorIdSuccess() throws Exception {
        CourseAssignedToUserDTO courseDTO = CourseAssignedToUserDTO.builder()
                .courseId(2L)
                .userId(1L)
                .build();

        when(courseService.findStudentsAssignedToCourseByInstructorId(courseDTO.getUserId(), courseDTO.getCourseId())).thenReturn(List.of(UserAssignedToCourseDTO.builder()
                .id(1L)
                .firstName("TEST")
                .lastName("TEST")
                .role("STUDENT")
                .email("test@email.com")
                .build()));

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/v1/course/instructor/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseDTO)));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].firstName").exists())
                .andExpect(jsonPath("$[0].lastName").exists())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].role").exists());

        verify(courseService, times(1)).findStudentsAssignedToCourseByInstructorId(courseDTO.getUserId(), courseDTO.getCourseId());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindLessonsByCourseAndStudentSuccess() throws Exception {

        CourseAssignedToUserDTO courseDTO = CourseAssignedToUserDTO.builder()
                .userId(1L)
                .courseId(2L)
                .build();

        when(courseService.findAllLessonsByCourseAssignedToUserId(courseDTO.getUserId(), courseDTO.getCourseId())).thenReturn(LessonsByCourseDTO.builder()
                .courseName("test course")
                .courseId(2L)
                .build());

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/v1/course/student/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseDTO)));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.courseName").exists())
                .andExpect(jsonPath("$.courseId").exists());

        verify(courseService, times(1)).findAllLessonsByCourseAssignedToUserId(courseDTO.getUserId(), courseDTO.getCourseId());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFinishCourseSuccess() throws Exception {
        CourseAssignedToUserDTO courseDTO = CourseAssignedToUserDTO.builder()
                .courseId(1L)
                .userId(1L)
                .build();

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/v1/course/finish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseDTO)));

        result.andExpect(status().isOk());

        verify(courseService, times(1)).finishCourse(courseDTO.getUserId(), courseDTO.getCourseId());
    }
}
