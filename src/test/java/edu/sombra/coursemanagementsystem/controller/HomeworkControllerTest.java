package edu.sombra.coursemanagementsystem.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkDTO;
import edu.sombra.coursemanagementsystem.dto.homework.HomeworkDTO;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.service.HomeworkService;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class HomeworkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HomeworkService homeworkService;

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testSetMarkSuccess() throws Exception {
        HomeworkDTO homeworkDTO = new HomeworkDTO();
        homeworkDTO.setHomeworkId(1L);
        homeworkDTO.setUserId(2L);
        homeworkDTO.setMark(90L);

        GetHomeworkDTO enrollmentGetDTO = GetHomeworkDTO.builder()
                .id(homeworkDTO.getHomeworkId())
                .userId(homeworkDTO.getUserId())
                .userEmail("test@email.com")
                .mark(homeworkDTO.getMark())
                .build();

        when(homeworkService.setMark(homeworkDTO.getUserId(), homeworkDTO.getHomeworkId(), homeworkDTO.getMark())).thenReturn(enrollmentGetDTO);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/homework/mark")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(homeworkDTO)));

        result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(enrollmentGetDTO.getId()))
                .andExpect(jsonPath("$.mark").value(enrollmentGetDTO.getMark()))
                .andExpect(jsonPath("$.userId").value(enrollmentGetDTO.getUserId()));

        verify(homeworkService, times(1)).setMark(homeworkDTO.getUserId(), homeworkDTO.getHomeworkId(), homeworkDTO.getMark());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testGetHomeworkByIdSuccess() throws Exception {
        Long homeworkId = 1L;

        when(homeworkService.findHomeworkById(homeworkId)).thenReturn(GetHomeworkDTO.builder()
                .userId(1L)
                .id(1L)
                .userEmail("test@email.com")
                .mark(80L)
                .build());

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/homework/{homeworkId}", homeworkId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userEmail").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.mark").exists());

        verify(homeworkService, times(1)).findHomeworkById(homeworkId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testGetAllHomeworksSuccess() throws Exception {
        List<GetHomeworkDTO> homeworkList = new ArrayList<>();
        GetHomeworkDTO homework1 = GetHomeworkDTO.builder()
                .id(1L)
                .mark(90L)
                .userId(2L)
                .userEmail("user@example.com")
                .lesson(new Lesson())
                .fileName("homework1.txt")
                .build();
        GetHomeworkDTO homework2 = GetHomeworkDTO.builder()
                .id(2L)
                .mark(85L)
                .userId(3L)
                .userEmail("anotheruser@example.com")
                .lesson(new Lesson())
                .fileName("homework2.txt")
                .build();
        homeworkList.add(homework1);
        homeworkList.add(homework2);

        when(homeworkService.getAllHomeworks()).thenReturn(homeworkList);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/homework")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].mark").value(90))
                .andExpect(jsonPath("$[0].userId").value(2))
                .andExpect(jsonPath("$[0].userEmail").value("user@example.com"))
                .andExpect(jsonPath("$[0].lesson").exists())
                .andExpect(jsonPath("$[0].fileName").value("homework1.txt"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].mark").value(85))
                .andExpect(jsonPath("$[1].userId").value(3))
                .andExpect(jsonPath("$[1].userEmail").value("anotheruser@example.com"))
                .andExpect(jsonPath("$[1].lesson").exists())
                .andExpect(jsonPath("$[1].fileName").value("homework2.txt"));

        verify(homeworkService, times(1)).getAllHomeworks();
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testGetAllHomeworksByUserSuccess() throws Exception {
        Long userId = 1L;
        List<GetHomeworkDTO> homeworkDTOList = Arrays.asList(
                GetHomeworkDTO.builder().id(1L).mark(90L).userId(1L).userEmail("user@example.com").fileName("homework1.txt").build(),
                GetHomeworkDTO.builder().id(2L).mark(85L).userId(1L).userEmail("user@example.com").fileName("homework2.txt").build()
        );

        when(homeworkService.getAllHomeworksByUser(userId)).thenReturn(homeworkDTOList);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/homework/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(homeworkDTOList)));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].mark").value(90L))
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].userEmail").value("user@example.com"))
                .andExpect(jsonPath("$[0].fileName").value("homework1.txt"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].mark").value(85L))
                .andExpect(jsonPath("$[1].userId").value(1L))
                .andExpect(jsonPath("$[1].userEmail").value("user@example.com"))
                .andExpect(jsonPath("$[1].fileName").value("homework2.txt"));

        verify(homeworkService, times(1)).getAllHomeworksByUser(userId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testDeleteHomeworkAPI() throws Exception {
        Long homeworkId = 1L;

        when(homeworkService.deleteHomework(homeworkId)).thenReturn("Homework deleted successfully!");

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/homework/{homeworkId}", homeworkId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(jsonPath("$").isString());

        verify(homeworkService, times(1)).deleteHomework(homeworkId);
    }
}
