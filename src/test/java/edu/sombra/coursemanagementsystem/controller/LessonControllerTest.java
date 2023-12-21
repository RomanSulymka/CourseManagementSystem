package edu.sombra.coursemanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.CreateLessonDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.LessonResponseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.UpdateLessonDTO;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.mapper.LessonMapper;
import edu.sombra.coursemanagementsystem.service.LessonService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LessonService lessonService;

    @MockBean
    private LessonMapper lessonMapper;

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testCreateLessonSuccess() throws Exception {
        CreateLessonDTO lessonDTO = new CreateLessonDTO();
        lessonDTO.setLessonName("Introduction to Spring");
        lessonDTO.setCourseId(1L);

        LessonResponseDTO lesson = LessonResponseDTO.builder()
                .id(1L)
                .name("Introduction to Spring")
                .course(mock(CourseResponseDTO.class))
                .build();

        when(lessonMapper.mapToResponseDTO(mock(Lesson.class), mock(CourseResponseDTO.class))).thenReturn(lesson);
        when(lessonService.save(lessonDTO)).thenReturn(lesson);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/lesson/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lessonDTO)));

        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Introduction to Spring"))
                .andExpect(jsonPath("$.course").exists());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testDeleteLessonSuccess() throws Exception {
        Long lessonId = 1L;

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/lesson/{id}", lessonId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(lessonService, times(1)).deleteLesson(lessonId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testGetLessonByIdSuccess() throws Exception {
        Long lessonId = 1L;

        LessonResponseDTO lesson = LessonResponseDTO.builder()
                .id(1L)
                .name("Introduction to Spring")
                .course(mock(CourseResponseDTO.class))
                .build();

        when(lessonMapper.mapToResponseDTO(mock(Lesson.class), mock(CourseResponseDTO.class))).thenReturn(lesson);
        when(lessonService.findById(lessonId, "admin@gmail.com")).thenReturn(lesson);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/lesson/{id}", lessonId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(lessonId))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.course").exists());

        verify(lessonService, times(1)).findById(lessonId, "admin@gmail.com");
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testGetAllLessonsSuccess() throws Exception {
        List<LessonResponseDTO> lessons = Arrays.asList(
                LessonResponseDTO.builder()
                        .id(1L)
                        .name("Introduction to Spring")
                        .course(mock(CourseResponseDTO.class))
                        .build(),
                LessonResponseDTO.builder()
                        .id(2L)
                        .name("Introduction to Scala")
                        .course(mock(CourseResponseDTO.class))
                        .build()
        );

        when(lessonService.findAllLessons("admin@gmail.com")).thenReturn(lessons);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/lesson/find-all")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Introduction to Spring"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Introduction to Scala"));

        verify(lessonService, times(1)).findAllLessons("admin@gmail.com");
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testGetAllLessonsByCourseIdSuccess() throws Exception {
        Long courseId = 1L;
        List<LessonResponseDTO> lessons = Arrays.asList(
                LessonResponseDTO.builder()
                        .id(1L)
                        .name("Introduction to Spring")
                        .course(mock(CourseResponseDTO.class))
                        .build(),
                LessonResponseDTO.builder()
                        .id(2L)
                        .name("Introduction to Scala")
                        .course(mock(CourseResponseDTO.class))
                        .build()
        );

        given(lessonService.findAllLessonsByCourse(courseId, "admin@gmail.com")).willReturn(lessons);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/lesson/find-all/{id}", courseId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Introduction to Spring"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Introduction to Scala"));

        verify(lessonService, times(1)).findAllLessonsByCourse(courseId, "admin@gmail.com");
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testEditLessonSuccess() throws Exception {
        LessonResponseDTO lesson = LessonResponseDTO.builder()
                .id(1L)
                .name("Introduction to Spring")
                .course(CourseResponseDTO.builder().courseId(2L).build())
                .build();

        UpdateLessonDTO updateLessonDTO = UpdateLessonDTO.builder()
                .id(1L)
                .name("Introduction to Spring")
                .courseId(2L)
                .build();

        when(lessonService.editLesson(updateLessonDTO)).thenReturn(lesson);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/lesson/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateLessonDTO)));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Introduction to Spring"))
                .andExpect(jsonPath("$.course.courseId").value(2));

        verify(lessonService, times(1)).editLesson(updateLessonDTO);
    }
}
