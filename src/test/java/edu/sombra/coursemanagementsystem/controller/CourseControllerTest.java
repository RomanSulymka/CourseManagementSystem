package edu.sombra.coursemanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.LessonsByCourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.UpdateCourseDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserAssignedToCourseDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
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

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testCreateCourseSuccess() throws Exception {
        CourseDTO courseDTO = CourseDTO.builder()
                .course(Course.builder().id(1L).name("Java learn").build())
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
                .started(true)
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

        Course course = Course.builder()
                .id(1L)
                .name("Java Programming")
                .status(CourseStatus.STOP)
                .startDate(LocalDate.of(2023, 1, 1))
                .started(true)
                .build();

        when(courseService.findById(courseId)).thenReturn(course);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/course/{id}", courseId));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(courseId))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.startDate").exists())
                .andExpect(jsonPath("$.started").exists());

        verify(courseService, times(1)).findById(courseId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindAllCoursesSuccess() throws Exception {
        List<Course> courses = Arrays.asList(
                Course.builder()
                        .id(1L)
                        .name("Java Programming")
                        .status(CourseStatus.STOP)
                        .startDate(LocalDate.of(2023, 1, 1))
                        .started(true)
                        .build(),
                Course.builder()
                        .id(2L)
                        .name("Scala Programming")
                        .status(CourseStatus.STARTED)
                        .startDate(LocalDate.of(2023, 1, 2))
                        .started(true)
                        .build()
        );

        when(courseService.findAllCourses()).thenReturn(courses);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/course/find-all")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].status").exists())
                .andExpect(jsonPath("$[1].startDate").exists())
                .andExpect(jsonPath("$[1].started").exists());

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
        Long courseId = 1L;
        String action = "start";

        Course course = Course.builder()
                .id(1L)
                .name("Java Programming")
                .status(CourseStatus.STARTED)
                .startDate(LocalDate.of(2023, 1, 1))
                .started(true)
                .build();

        when(courseService.startOrStopCourse(courseId, action)).thenReturn(course);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/course/{courseId}/{action}", courseId, action));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(courseId))
                .andExpect(jsonPath("$.status").value("STARTED"))
                .andExpect(jsonPath("$.name").value("Java Programming"));

        verify(courseService, times(1)).startOrStopCourse(courseId, action);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindAllLessonsByCourseSuccess() throws Exception {
        Long courseId = 1L;
        Lesson lesson1 = Lesson.builder().id(1L).name("Lesson 1").course(Course.builder().id(courseId).build()).build();
        Lesson lesson2 = Lesson.builder().id(2L).name("Lesson 2").course(Course.builder().id(courseId).build()).build();
        List<Lesson> lessons = Arrays.asList(lesson1, lesson2);

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
    void testFindCoursesByInstructorIdSuccess() throws Exception {
        Long instructorId = 1L;
        given(courseService.findCoursesByInstructorId(instructorId)).willReturn(Collections.singletonList(
                Course.builder().id(1L).name("Java Programming").status(CourseStatus.FINISHED).started(true).build()
        ));

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/course/instructor/{instructorId}", instructorId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Java Programming"))
                .andExpect(jsonPath("$[0].status").value("FINISHED"))
                .andExpect(jsonPath("$[0].started").value(true));

        verify(courseService, times(1)).findCoursesByInstructorId(instructorId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindUsersAssignedToCourseByInstructorIdSuccess() throws Exception {
        Long instructorId = 1L;
        Long courseId = 2L;

        when(courseService.findStudentsAssignedToCourseByInstructorId(instructorId, courseId)).thenReturn(List.of(UserAssignedToCourseDTO.builder()
                .id(1L)
                .firstName("TEST")
                .lastName("TEST")
                .role("STUDENT")
                .email("test@email.com")
                .build()));

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/v1/course/instructor/{instructorId}/{courseId}", instructorId, courseId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].firstName").exists())
                .andExpect(jsonPath("$[0].lastName").exists())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].role").exists());

        verify(courseService, times(1)).findStudentsAssignedToCourseByInstructorId(instructorId, courseId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindCoursesByStudentIdSuccess() throws Exception {
        Long studentId = 1L;

        Course course = Course.builder()
                .id(1L)
                .name("Java Programming")
                .status(CourseStatus.STARTED)
                .startDate(LocalDate.of(2023, 1, 1))
                .started(true)
                .build();

        when(courseService.findCoursesByUserId(studentId)).thenReturn(List.of(course));

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/course/student/{studentId}", studentId));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].status").exists())
                .andExpect(jsonPath("$[0].startDate").exists())
                .andExpect(jsonPath("$[0].started").exists());

        verify(courseService, times(1)).findCoursesByUserId(studentId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindLessonsByCourseAndStudentSuccess() throws Exception {
        Long studentId = 1L;
        Long courseId = 2L;

        when(courseService.findAllLessonsByCourseAssignedToUserId(studentId, courseId)).thenReturn(LessonsByCourseDTO.builder()
                .courseName("test course")
                .courseId(2L)
                .build());

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/v1/course/student/lessons/{studentId}/{courseId}", studentId, courseId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.courseName").exists())
                .andExpect(jsonPath("$.courseId").exists());

        verify(courseService, times(1)).findAllLessonsByCourseAssignedToUserId(studentId, courseId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFinishCourseSuccess() throws Exception {
        Long studentId = 1L;
        Long courseId = 2L;

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/v1/course/finish/{studentId}/{courseId}", studentId, courseId));

        result.andExpect(status().isOk());

        verify(courseService, times(1)).finishCourse(studentId, courseId);
    }
}
