package edu.sombra.coursemanagementsystem.e2e.coursefeedback;

import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.dto.feedback.CourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.dto.feedback.GetCourseFeedbackDTO;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminCourseFeedbackControllerE2ETest {

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
    void testAddFeedback() {
        CourseFeedbackDTO courseFeedbackDTO = new CourseFeedbackDTO();
        courseFeedbackDTO.setFeedbackText("This is a great course.");
        courseFeedbackDTO.setCourseId(2L);
        courseFeedbackDTO.setStudentId(4L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<CourseFeedbackDTO> requestEntity = new HttpEntity<>(courseFeedbackDTO, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/feedback"),
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testAddFeedback_EntityExistException() {
        CourseFeedbackDTO courseFeedbackDTO = new CourseFeedbackDTO();
        courseFeedbackDTO.setFeedbackText("This is a great course.");
        courseFeedbackDTO.setCourseId(2L);
        courseFeedbackDTO.setStudentId(4L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<CourseFeedbackDTO> requestEntity = new HttpEntity<>(courseFeedbackDTO, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/feedback"),
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void testEditFeedback() {
        CourseFeedbackDTO courseFeedbackDTO = new CourseFeedbackDTO();
        courseFeedbackDTO.setId(1L);
        courseFeedbackDTO.setFeedbackText("Great!!");
        courseFeedbackDTO.setCourseId(1L);
        courseFeedbackDTO.setStudentId(1L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<CourseFeedbackDTO> requestEntity = new HttpEntity<>(courseFeedbackDTO, headers);

        ResponseEntity<GetCourseFeedbackDTO> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/feedback"),
                HttpMethod.PUT,
                requestEntity,
                GetCourseFeedbackDTO.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testGetAllFeedbacks() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setAccept(List.of(org.springframework.http.MediaType.APPLICATION_JSON));

        ResponseEntity<List<GetCourseFeedbackDTO>> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/feedback"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testGetFeedbackById() {
        Long feedbackId = 2L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<GetCourseFeedbackDTO> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/feedback/{id}", feedbackId),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                GetCourseFeedbackDTO.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testGetFeedbackByIdThrowNotFoundException() {
        Long feedbackId = 10000L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/feedback/{id}", feedbackId),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void testDeleteFeedback() {
        Long feedbackId = 1L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/feedback/{id}", feedbackId),
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
