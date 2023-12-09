/*
package edu.sombra.coursemanagementsystem.e2e.course;

import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.entity.Course;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminCourseControllerE2ETest {

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
    void testCreateCourse() {

    }

    @Test
    void testEditCourse() {

    }

    @Test
    void testGetCourseById() {
        long courseId = 1L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<Course> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/course/" + courseId),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Course.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(courseId, responseEntity.getBody().getId());
    }

    @Test
    void testFindAllCourses() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<List<Course>> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/course/find-all"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Course>>() {}
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertFalse(responseEntity.getBody().isEmpty());
    }

    @Test
    void testDeleteCourse() {
        long courseId = 41L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<Boolean> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/course/" + courseId),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Boolean.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }


    private String buildUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
*/
