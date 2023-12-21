package edu.sombra.coursemanagementsystem.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.RegisterDTO;
import edu.sombra.coursemanagementsystem.service.auth.AuthenticateService;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticateService authenticateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authenticate_ValidRequest_ReturnsOk() throws Exception {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO("username", "password");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationDTO)))
                .andExpect(status().isOk());
        verify(authenticateService).authenticate(authenticationDTO);
    }

    @Test
    void registerUser_ValidRequest_ReturnsOk() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO("firstName", "lastName", "email", "password");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void refreshJWTToken_ValidRequest_ReturnsOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/refresh-token"))
                .andExpect(status().isOk());
    }
}
