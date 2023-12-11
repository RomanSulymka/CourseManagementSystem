/*
package edu.sombra.coursemanagementsystem.e2e.homework;

import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkDTO;
import edu.sombra.coursemanagementsystem.dto.homework.HomeworkDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminHomeworkControllerE2ETest {

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
    void testSetMark() {
        HomeworkDTO homeworkDTO = new HomeworkDTO();
        homeworkDTO.setHomeworkId(32L);
        homeworkDTO.setUserId(2L);
        homeworkDTO.setMark(90L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<HomeworkDTO> requestEntity = new HttpEntity<>(homeworkDTO, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/homework/mark",
                HttpMethod.PUT,
                requestEntity,
                String.class
        );

        assertEquals("Mark saved successfully", responseEntity.getBody());
    }

    @Test
    void testSetMarkWhenUserIsNotAssignedToCourse() {
        HomeworkDTO homeworkDTO = new HomeworkDTO();
        homeworkDTO.setHomeworkId(1L);
        homeworkDTO.setUserId(2L);
        homeworkDTO.setMark(90L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<HomeworkDTO> requestEntity = new HttpEntity<>(homeworkDTO, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/homework/mark",
                HttpMethod.PUT,
                requestEntity,
                String.class
        );

        assertEquals("{\"statusCode\":400,\"message\":\"User isn't assigned to this course\"}", responseEntity.getBody());
    }

    @Test
    void testGetHomework() {
        Long homeworkId = 1L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<GetHomeworkDTO> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/homework/" + homeworkId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                GetHomeworkDTO.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testGetAllHomeworks() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<List<GetHomeworkDTO>> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/homework",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testGetAllHomeworksByUser() {
        Long userId = 1L;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<List<GetHomeworkDTO>> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/homework/user/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testDeleteHomework() {
        Long homeworkId = 27L;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/homework/" + homeworkId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals("Homework deleted successfully!", responseEntity.getBody());
    }
}
*/
