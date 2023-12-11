/*
package edu.sombra.coursemanagementsystem.e2e.lesson;

import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.dto.lesson.CreateLessonDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Lesson;
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

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminLessonControllerE2ETest {

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
    void testCreateLesson() {
        CreateLessonDTO lessonDTO = new CreateLessonDTO();
        lessonDTO.setLessonName("Introduction to Spring");
        lessonDTO.setCourseId(1L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<CreateLessonDTO> requestEntity = new HttpEntity<>(lessonDTO, headers);

        ResponseEntity<Lesson> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/lesson/create"),
                HttpMethod.POST,
                requestEntity,
                Lesson.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    void testDeleteLesson() {
        long lessonId = 372L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/lesson/" + lessonId),
                HttpMethod.DELETE,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Lesson deleted successfully", responseEntity.getBody());
    }

    @Test
    void testDeleteLesson_EntityNotFound() {
        long lessonId = 1000L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                buildUrl("/api/v1/lesson/" + lessonId),
                HttpMethod.DELETE,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void testGetLessonById() {
        long lessonId = 352L;

        HttpHeaders getHeaders = new HttpHeaders();
        getHeaders.setBearerAuth(jwtToken);
        HttpEntity<Void> getRequestEntity = new HttpEntity<>(getHeaders);

        ResponseEntity<Lesson> getResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/lesson/" + lessonId),
                HttpMethod.GET,
                getRequestEntity,
                Lesson.class
        );

        assertEquals(HttpStatus.OK, getResponseEntity.getStatusCode());
        assertNotNull(getResponseEntity.getBody());
    }

    @Test
    void testGetLessonById_EntityNotFound() {
        long lessonId = 1000L;

        HttpHeaders getHeaders = new HttpHeaders();
        getHeaders.setBearerAuth(jwtToken);
        HttpEntity<Void> getRequestEntity = new HttpEntity<>(getHeaders);

        ResponseEntity<Lesson> getResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/lesson/" + lessonId),
                HttpMethod.GET,
                getRequestEntity,
                Lesson.class
        );

        assertEquals(HttpStatus.NOT_FOUND, getResponseEntity.getStatusCode());
        assertNotNull(getResponseEntity.getBody());
    }

    @Test
    void testGetAllLessons() {
        HttpHeaders headersWithLessons = new HttpHeaders();
        headersWithLessons.setBearerAuth(jwtToken);
        HttpEntity<Void> requestEntityWithLessons = new HttpEntity<>(headersWithLessons);

        ResponseEntity<List> responseEntityWithLessons = restTemplate.exchange(
                buildUrl("/api/v1/lesson/find-all"),
                HttpMethod.GET,
                requestEntityWithLessons,
                List.class
        );

        assertEquals(HttpStatus.OK, responseEntityWithLessons.getStatusCode());
        assertNotNull(responseEntityWithLessons.getBody());
        assertFalse(responseEntityWithLessons.getBody().isEmpty());
    }

    @Test
    void testGetAllLessonsByCourseId_ListEmpty() {
        long courseIdWithoutLessons = 1000L;

        HttpHeaders headersNoLessons = new HttpHeaders();
        headersNoLessons.setBearerAuth(jwtToken);
        HttpEntity<Void> requestEntityNoLessons = new HttpEntity<>(headersNoLessons);

        ResponseEntity<List> responseEntityNoLessons = restTemplate.exchange(
                buildUrl("/api/v1/lesson/find-all/" + courseIdWithoutLessons),
                HttpMethod.GET,
                requestEntityNoLessons,
                List.class
        );

        assertEquals(HttpStatus.OK, responseEntityNoLessons.getStatusCode());
        assertNotNull(responseEntityNoLessons.getBody());
        assertTrue(responseEntityNoLessons.getBody().isEmpty());
    }

    @Test
    void testGetAllLessonsByCourseId() {

        long courseIdWithLessons = 1L;

        HttpHeaders headersWithLessons = new HttpHeaders();
        headersWithLessons.setBearerAuth(jwtToken);
        HttpEntity<Void> requestEntityWithLessons = new HttpEntity<>(headersWithLessons);

        ResponseEntity<List> responseEntityWithLessons = restTemplate.exchange(
                buildUrl("/api/v1/lesson/find-all/" + courseIdWithLessons),
                HttpMethod.GET,
                requestEntityWithLessons,
                List.class
        );

        assertEquals(HttpStatus.OK, responseEntityWithLessons.getStatusCode());
        assertNotNull(responseEntityWithLessons.getBody());
        assertFalse(responseEntityWithLessons.getBody().isEmpty());
    }

    @Test
    void testEditLesson_BadRequest() {
        CreateLessonDTO lessonDTO = new CreateLessonDTO();

        HttpHeaders createHeaders = new HttpHeaders();
        createHeaders.setBearerAuth(jwtToken);
        HttpEntity<CreateLessonDTO> createRequestEntity = new HttpEntity<>(lessonDTO, createHeaders);

        ResponseEntity<Lesson> createResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/lesson/create"),
                HttpMethod.POST,
                createRequestEntity,
                Lesson.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, createResponseEntity.getStatusCode());
        assertNotNull(createResponseEntity.getBody());
    }

    @Test
    void testEditLesson() {
        Lesson editedLesson = Lesson.builder()
                .id(352L)
                .course(Course.builder()
                        .id(1L)
                        .build())
                .name("Edited Lesson Name")
                .build();

        HttpHeaders editHeaders = new HttpHeaders();
        editHeaders.setBearerAuth(jwtToken);
        HttpEntity<Lesson> editRequestEntity = new HttpEntity<>(editedLesson, editHeaders);

        ResponseEntity<Lesson> editResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/lesson/edit"),
                HttpMethod.PUT,
                editRequestEntity,
                Lesson.class
        );

        assertEquals(HttpStatus.OK, editResponseEntity.getStatusCode());
        assertNotNull(editResponseEntity.getBody());

        assertEquals("Edited Lesson Name", editResponseEntity.getBody().getName());
    }

    private String buildUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
*/
