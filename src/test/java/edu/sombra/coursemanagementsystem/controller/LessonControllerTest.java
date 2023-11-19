package edu.sombra.coursemanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sombra.coursemanagementsystem.dto.lesson.CreateLessonDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Lesson;
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

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testCreateLessonSuccess() throws Exception {
        CreateLessonDTO lessonDTO = new CreateLessonDTO();
        lessonDTO.setLessonName("Introduction to Spring");
        lessonDTO.setCourseId(1L);

        Lesson lesson = Lesson.builder()
                .id(1L)
                .name("Introduction to Spring")
                .course(mock(Course.class))
                .build();

        when(lessonService.save(lessonDTO)).thenReturn(lesson);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/lesson/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lessonDTO)));

        result.andExpect(status().isOk())
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
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(jsonPath("$").isString());

        verify(lessonService, times(1)).deleteLesson(lessonId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testGetLessonByIdSuccess() throws Exception {
        Long lessonId = 1L;

        Lesson lesson = Lesson.builder()
                .id(1L)
                .name("Introduction to Spring")
                .course(mock(Course.class))
                .build();

        when(lessonService.findById(lessonId)).thenReturn(lesson);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/lesson/{id}", lessonId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(lessonId))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.course").exists());

        verify(lessonService, times(1)).findById(lessonId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testGetAllLessonsSuccess() throws Exception {
        List<Lesson> lessons = Arrays.asList(
                Lesson.builder()
                        .id(1L)
                        .name("Introduction to Spring")
                        .course(mock(Course.class))
                        .build(),
                Lesson.builder()
                        .id(2L)
                        .name("Introduction to Scala")
                        .course(mock(Course.class))
                        .build()
        );

        when(lessonService.findAllLessons()).thenReturn(lessons);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/lesson/find-all")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Introduction to Spring"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Introduction to Scala"));

        verify(lessonService, times(1)).findAllLessons();
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testGetAllLessonsByCourseIdSuccess() throws Exception {
        Long courseId = 1L;
        List<Lesson> lessons = Arrays.asList(
                Lesson.builder()
                        .id(1L)
                        .name("Introduction to Spring")
                        .course(mock(Course.class))
                        .build(),
                Lesson.builder()
                        .id(2L)
                        .name("Introduction to Scala")
                        .course(mock(Course.class))
                        .build()
        );

        given(lessonService.findAllLessonsByCourse(courseId)).willReturn(lessons);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/lesson/find-all/{id}", courseId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Introduction to Spring"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Introduction to Scala"));

        verify(lessonService, times(1)).findAllLessonsByCourse(courseId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testEditLessonSuccess() throws Exception {
        Lesson lesson = Lesson.builder()
                .id(1L)
                .name("Introduction to Spring")
                .course(Course.builder().id(2L).build())
                .build();

        when(lessonService.editLesson(lesson)).thenReturn(lesson);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/lesson/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lesson)));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Introduction to Spring"))
                .andExpect(jsonPath("$.course.id").value(2));

        verify(lessonService, times(1)).editLesson(lesson);
    }
}
