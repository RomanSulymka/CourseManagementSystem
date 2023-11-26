package edu.sombra.coursemanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sombra.coursemanagementsystem.dto.user.ResetPasswordDTO;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("johndoe@example.com");
        testUser.setPassword("password");
        testUser.setRole(RoleEnum.STUDENT);
    }

/*    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testCreateUserSuccess() throws Exception {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .role(RoleEnum.STUDENT)
                .build();

        when(userService.createUser(user)).thenReturn(testUser);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }*/

/*    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testUpdateUserSuccess() throws Exception {
        when(userService.updateUser(testUser)).thenReturn(testUser);

        mockMvc.perform(put("/api/v1/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }*/

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testResetPasswordSuccess() throws Exception {
        ResetPasswordDTO resetPasswordDTO = ResetPasswordDTO.builder()
                .newPassword("12342")
                .email("user@email.com")
                .build();

        when(userService.resetPassword(resetPasswordDTO)).thenReturn("Password changed!");

        String resetPasswordJson = objectMapper.writeValueAsString(resetPasswordDTO);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetPasswordJson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(jsonPath("$").value("Password changed!"));

        verify(userService, times(1)).resetPassword(resetPasswordDTO);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindUserById() throws Exception {
        Long userId = 1L;
        User mockUser = new User();
                mockUser.setId(1L);
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setEmail("johndoe@example.com");
        mockUser.setPassword("password");
        mockUser.setRole(RoleEnum.STUDENT);
        when(userService.findUserById(userId)).thenReturn(mockUser);

        ResultActions result = mockMvc.perform(get("/api/v1/user/id/{id}", userId));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("johndoe@example.com"));
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindUserByEmail() throws Exception {
        String userEmail = "johndoe@example.com";
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setEmail("johndoe@example.com");
        mockUser.setPassword("password");
        mockUser.setRole(RoleEnum.STUDENT);
        when(userService.findUserByEmail(userEmail)).thenReturn(mockUser);

        ResultActions result = mockMvc.perform(get("/api/v1/user/email/{email}", userEmail)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value(userEmail));
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindAllUsersSuccess() throws Exception {
        when(userService.findAllUsers()).thenReturn(Collections.singletonList(testUser));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user/find-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testDeleteUser() throws Exception {
        when(userService.deleteUser(1L)).thenReturn("User deleted successfully");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }
}
