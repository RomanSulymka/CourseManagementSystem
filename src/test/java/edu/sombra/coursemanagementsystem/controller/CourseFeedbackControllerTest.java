package edu.sombra.coursemanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sombra.coursemanagementsystem.dto.feedback.CourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.dto.feedback.GetCourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.service.CourseFeedbackService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class CourseFeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseFeedbackService courseFeedbackService;

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testAddFeedbackAPI() throws Exception {
        CourseFeedbackDTO feedbackDTO = new CourseFeedbackDTO();
        feedbackDTO.setId(3L);
        feedbackDTO.setFeedbackText("Great course!");
        feedbackDTO.setStudentId(1L);
        feedbackDTO.setCourseId(5L);

        GetCourseFeedbackDTO getCourseFeedbackDTO = GetCourseFeedbackDTO.builder()
                .id(3L)
                .feedbackText("Great course!")
                .studentId(1L)
                .course(mock(Course.class))
                .build();

        when(courseFeedbackService.create(feedbackDTO, "admin@gmail.com")).thenReturn(getCourseFeedbackDTO);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feedbackDTO)));

        result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.feedbackText").value(getCourseFeedbackDTO.getFeedbackText()))
                .andExpect(jsonPath("$.id").value(getCourseFeedbackDTO.getId()));
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testEditFeedbackAPI() throws Exception {
        CourseFeedbackDTO feedbackDTO = new CourseFeedbackDTO();
        feedbackDTO.setFeedbackText("Updated comment");
        feedbackDTO.setStudentId(5L);
        feedbackDTO.setCourseId(1L);

        GetCourseFeedbackDTO expected = GetCourseFeedbackDTO.builder()
                .id(4L)
                .feedbackText("Updated comment")
                .studentId(5L)
                .course(mock(Course.class))
                .build();

        when(courseFeedbackService.edit(feedbackDTO, "admin@gmail.com")).thenReturn(expected);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feedbackDTO)));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.feedbackText").value("Updated comment"));
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testGetAllFeedbacksAPI() throws Exception {
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/feedback"));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testGetFeedbackByIdAPI() throws Exception {
        Long feedbackId = 4L;

        GetCourseFeedbackDTO feedbackDTO = GetCourseFeedbackDTO.builder()
                .id(4L)
                .feedbackText("Great course!")
                .studentId(1L)
                .course(mock(Course.class))
                .build();

        when(courseFeedbackService.findCourseFeedbackById(feedbackId, "admin@gmail.com")).thenReturn(feedbackDTO);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/feedback/{id}", feedbackId));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(feedbackId));
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testDeleteFeedbackAPI() throws Exception {
        Long feedbackId = 8L;

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/feedback/{id}", feedbackId));

        result.andExpect(status().isNoContent());

        verify(courseFeedbackService, times(1)).delete(feedbackId);
    }
}