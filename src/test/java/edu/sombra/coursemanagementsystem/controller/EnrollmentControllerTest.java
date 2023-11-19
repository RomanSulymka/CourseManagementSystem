package edu.sombra.coursemanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentApplyForCourseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentGetByNameDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentUpdateDTO;
import edu.sombra.coursemanagementsystem.service.EnrollmentService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnrollmentService enrollmentService;

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testSetInstructorSuccess() throws Exception {
        EnrollmentDTO enrollmentDTO = EnrollmentDTO.builder()
                .userEmail("instructo@email.com")
                .courseName("test course")
                .build();

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/enrollment/instructor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enrollmentDTO)));

        result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(jsonPath("$").isString());

        verify(enrollmentService, times(1)).assignInstructor(enrollmentDTO);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testApplyForCourseSuccess() throws Exception {
        EnrollmentApplyForCourseDTO enrollmentDTO = EnrollmentApplyForCourseDTO.builder()
                .userId(4L)
                .courseName("test course")
                .build();

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/enrollment/user/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enrollmentDTO)));

        result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(jsonPath("$").isString());

        verify(enrollmentService, times(1)).applyForCourse(enrollmentDTO, "admin@gmail.com");
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testGetEnrollmentByIdSuccess() throws Exception {
        Long enrollmentId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/enrollment/{id}", 1))
                .andExpect(status().isOk());

        verify(enrollmentService, times(1)).findEnrolmentById(enrollmentId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testGetEnrollmentByNameSuccess() throws Exception {
        String enrollmentName = "Test name";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/enrollment/by-name/{name}", enrollmentName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(enrollmentService, times(1)).findEnrolmentByCourseName(enrollmentName);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testUpdateEnrollmentSuccess() throws Exception {
        EnrollmentUpdateDTO updateDTO = new EnrollmentUpdateDTO();
        updateDTO.setId(8L);
        updateDTO.setUserId(2L);
        updateDTO.setCourseId(3L);

        EnrollmentGetByNameDTO enrollmentGetByNameDTO = mock(EnrollmentGetByNameDTO.class);

        when(enrollmentService.updateEnrollment(updateDTO)).thenReturn(enrollmentGetByNameDTO);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/enrollment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(enrollmentService, times(1)).updateEnrollment(updateDTO);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testDeleteEnrollmentSuccess() throws Exception {
        Long enrollmentId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/enrollment/{id}", enrollmentId))
                .andExpect(status().isOk());

        verify(enrollmentService, times(1)).removeUserFromCourse(enrollmentId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testGetListEnrollmentsByUserSuccess() throws Exception {
        Long userId = 1L;
        List<String> courses = Arrays.asList("Course1", "Course2", "Course3");

        when(enrollmentService.findAllCoursesByUser(userId)).thenReturn(courses);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/enrollment/user/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("Course1"))
                .andExpect(jsonPath("$[1]").value("Course2"))
                .andExpect(jsonPath("$[2]").value("Course3"));

        verify(enrollmentService, times(1)).findAllCoursesByUser(userId);
    }
}
