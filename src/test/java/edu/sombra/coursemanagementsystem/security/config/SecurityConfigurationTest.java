package edu.sombra.coursemanagementsystem.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.CreateLessonDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    private CourseDTO createCourseDTO() {
        return CourseDTO.builder()
                .course(Course.builder()
                        .name("Training2")
                        .startDate(LocalDate.parse("2023-12-01"))
                        .status(CourseStatus.WAIT)
                        .build())
                .instructorEmail("instructor@gmail.com")
                .numberOfLessons(10L)
                .build();
    }

    //FIXME
/*    @ParameterizedTest
    @MethodSource("createCourseDTO")
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testAdminAccess(CourseDTO courseDTO) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/course/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }*/

    @Test
    @WithMockUser(username = "instructor@gmail.com", roles = "INSTRUCTOR")
    void testInstructorAccess() throws Exception {
        CreateLessonDTO courseDTO =  new CreateLessonDTO();
        courseDTO.setCourseId(1L);
        courseDTO.setLessonName("Test English lesson");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/lesson/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "student@gmail.com", roles = "STUDENT")
    void testStudentAccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/homework/{homeworkId}", 1))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
