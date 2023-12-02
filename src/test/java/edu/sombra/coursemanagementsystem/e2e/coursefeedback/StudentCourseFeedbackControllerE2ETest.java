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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StudentCourseFeedbackControllerE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String jwtToken;

    @BeforeEach
    void login() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO("student@gmail.com", "studentPass");

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

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    void testEditFeedback() {
        CourseFeedbackDTO courseFeedbackDTO = new CourseFeedbackDTO();
        courseFeedbackDTO.setId(6L);
        courseFeedbackDTO.setFeedbackText("Great!!");
        courseFeedbackDTO.setCourseId(2L);
        courseFeedbackDTO.setStudentId(4L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<CourseFeedbackDTO> requestEntity = new HttpEntity<>(courseFeedbackDTO, headers);

        ResponseEntity<GetCourseFeedbackDTO> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/feedback"),
                HttpMethod.PUT,
                requestEntity,
                GetCourseFeedbackDTO.class
        );

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    void testGetAllFeedbacks() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<List> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/feedback"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }


    @Test
    void testGetFeedbackById() {
        Long feedbackId = 6L;

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
        Long feedbackId = 1L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<GetCourseFeedbackDTO> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/feedback/{id}", feedbackId),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                GetCourseFeedbackDTO.class
        );

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void testDeleteFeedback() {
        Long feedbackId = 6L;

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

    @Test
    void testDeleteFeedback_NotFound() {
        Long feedbackId = 1000L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/feedback/{id}", feedbackId),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    private String buildUrl(String path, Object... uriVariables) {
        return UriComponentsBuilder.fromUriString("http://localhost:" + port + path).buildAndExpand(uriVariables).toUriString();
    }
}
