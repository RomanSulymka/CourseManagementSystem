package edu.sombra.coursemanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sombra.coursemanagementsystem.dto.user.CreateUserDTO;
import edu.sombra.coursemanagementsystem.dto.user.ResetPasswordDTO;
import edu.sombra.coursemanagementsystem.dto.user.UpdateUserDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserResponseDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private UserResponseDTO testUserResponse;

    @BeforeEach
    void setUp() {
        testUserResponse = new UserResponseDTO();
        testUserResponse.setId(1L);
        testUserResponse.setFirstName("John");
        testUserResponse.setLastName("Doe");
        testUserResponse.setEmail("john.doe@example.com");
        testUserResponse.setPassword("password");
        testUserResponse.setRole(RoleEnum.STUDENT);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testCreateUserSuccess() throws Exception {
        CreateUserDTO user = CreateUserDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .role(RoleEnum.STUDENT)
                .build();

        when(userService.createUser(user)).thenReturn(testUserResponse);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)));

        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testUpdateUserSuccess() throws Exception {
        UpdateUserDTO updateTestUser = new UpdateUserDTO();
        updateTestUser.setId(1L);
        updateTestUser.setFirstName("John");
        updateTestUser.setLastName("Doe");
        updateTestUser.setEmail("john.doe@example.com");
        updateTestUser.setRole(RoleEnum.STUDENT);

        when(userService.updateUser(updateTestUser, "admin@gmail.com")).thenReturn(mock(UserResponseDTO.class));

        mockMvc.perform(put("/api/v1/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserResponse)))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUser(updateTestUser, "admin@gmail.com");
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testResetPasswordSuccess() throws Exception {
        ResetPasswordDTO resetPasswordDTO = ResetPasswordDTO.builder()
                .id(1L)
                .newPassword("12342")
                .email("user@email.com")
                .build();

        when(userService.resetPassword(resetPasswordDTO, "admin@gmail.com")).thenReturn("Password changed!");

        String resetPasswordJson = objectMapper.writeValueAsString(resetPasswordDTO);

        mockMvc.perform(put("/api/v1/user/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetPasswordJson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(jsonPath("$").value("Password changed!"));

        verify(userService, times(1)).resetPassword(resetPasswordDTO, "admin@gmail.com");
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindUserById() throws Exception {
        Long userId = 1L;
        UserResponseDTO mockUser = new UserResponseDTO();
        mockUser.setId(1L);
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setEmail("johndoe@example.com");
        mockUser.setPassword("password");
        mockUser.setRole(RoleEnum.STUDENT);
        when(userService.findUserById(userId)).thenReturn(mockUser);

        ResultActions result = mockMvc.perform(get("/api/v1/user/{id}", userId));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("johndoe@example.com"));
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testFindUserByEmail() throws Exception {
        String userEmail = "admin@gmail.com";

        UserDTO userDTO = UserDTO.builder()
                .email(userEmail)
                .build();

        UserResponseDTO mockUser = new UserResponseDTO();
        mockUser.setId(1L);
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setEmail("admin@gmail.com");
        mockUser.setPassword("password");
        mockUser.setRole(RoleEnum.STUDENT);
        when(userService.findUserByEmail(userEmail)).thenReturn(mockUser);

        ResultActions result = mockMvc.perform(post("/api/v1/user/email", userEmail)
                .content(objectMapper.writeValueAsString(userDTO))
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
        UserResponseDTO userResponseDTO = UserResponseDTO.builder().id(1L).role(RoleEnum.STUDENT).build();

        when(userService.findAllUsers()).thenReturn(Collections.singletonList(userResponseDTO));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user/find-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testDeleteUser() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/user/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(userService, times(1)).deleteUser(1L);
    }
}
