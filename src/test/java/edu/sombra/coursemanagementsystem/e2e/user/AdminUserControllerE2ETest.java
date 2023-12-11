/*
package edu.sombra.coursemanagementsystem.e2e.user;


import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.dto.user.ResetPasswordDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserDTO;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminUserControllerE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String jwtToken;

    @BeforeEach
    void login() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO("admin@gmail.com", "adminPAss");

        ResponseEntity<AuthenticationResponse> responseEntity = restTemplate.postForEntity(
                "/api/v1/auth/authenticate",
                authenticationDTO,
                AuthenticationResponse.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        AuthenticationResponse authenticationResponse = responseEntity.getBody();
        jwtToken = Objects.requireNonNull(authenticationResponse).getAccessToken();
    }

    @AfterEach
    void logout() {
        restTemplate.getForEntity("/api/v1/auth/logout", Void.class);
    }

    @Test
    void testCreateUser() {
        User user = new User();
        user.setLastName("testUser");
        user.setFirstName("testUser");
        user.setEmail("testinstructor@example.com");
        user.setPassword("password");
        user.setRole(RoleEnum.INSTRUCTOR);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<User> requestEntity = new HttpEntity<>(user, headers);

        ResponseEntity<User> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/user/create"),
                HttpMethod.POST,
                requestEntity,
                User.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    void testUpdateUser() {
        User user = User.builder()
                .id(3L)
                .lastName("instructor1")
                .firstName("user3 last name")
                .password("password3")
                .email("instructor1@example.com")
                .role(RoleEnum.INSTRUCTOR)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<User> responseEntity = restTemplate.exchange(
                "/api/v1/user/update",
                HttpMethod.PUT,
                new HttpEntity<>(user, headers),
                User.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testAssignNewRole() {
        UserDTO userDTO = UserDTO.builder()
                .email("user2@example.com")
                .role(RoleEnum.ADMIN)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "/api/v1/user/assign-role",
                HttpMethod.POST,
                new HttpEntity<>(userDTO, headers),
                String.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testResetPassword() {
        ResetPasswordDTO resetPasswordDTO = ResetPasswordDTO.builder()
                .newPassword("12342")
                .email("user2@example.com")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "/api/v1/user/reset-password",
                HttpMethod.PUT,
                new HttpEntity<>(resetPasswordDTO, headers),
                String.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testFindUserById() {
        Long userId = 1L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<User> responseEntity = restTemplate.exchange(
                "/api/v1/user/id/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                User.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testFindUserByEmail() {
        String userEmail = "user2@example.com";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<User> responseEntity = restTemplate.exchange(
                "/api/v1/user/email/" + userEmail,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                User.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testFindAllUsers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<List> responseEntity = restTemplate.exchange(
                "/api/v1/user/find-all",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testDeleteUser() {
        long userId = 2;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "/api/v1/user/" + userId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }


    private String buildUrl(String path, Object... uriVariables) {
        return UriComponentsBuilder.fromUriString("http://localhost:" + port + path).buildAndExpand(uriVariables).toUriString();
    }
}
*/
