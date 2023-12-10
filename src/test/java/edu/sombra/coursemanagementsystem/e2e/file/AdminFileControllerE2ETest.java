package edu.sombra.coursemanagementsystem.e2e.file;

import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.dto.file.FileResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminFileControllerE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

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
    void testFileUpload() {
        String filePath = "output.csv";

        Long userId = 1L;
        Long lessonId = 2L;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(jwtToken);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(filePath));
        body.add("lessonId", userId);
        body.add("userId", lessonId);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String uploadUrl = buildUrl("/api/v1/files/upload", userId, lessonId);

        ResponseEntity<FileResponseDTO> responseEntity = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                requestEntity,
                FileResponseDTO.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(filePath, responseEntity.getBody().getName());
    }

    @Test
    void testDownloadFile() {
        String filePath = "file2.txt";
        Long fileId = 2L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        String downloadUrl = buildUrl("/api/v1/files/download/{lessonId}", fileId);

        ResponseEntity<Resource> responseEntity = restTemplate.exchange(
                downloadUrl,
                HttpMethod.GET,
                requestEntity,
                Resource.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(filePath, Objects.requireNonNull(responseEntity.getBody()).getFilename());
    }

    @Test
    void testDeleteFile() {
        Long fileId = 1L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        String deleteUrl = buildUrl("/api/v1/files/{fileId}", fileId);

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                deleteUrl,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testDeleteFileWithUnauthorizedUser() {
        Long fileId = 1L;

        String deleteUrl = buildUrl("/api/v1/files/{fileId}", fileId);

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                deleteUrl,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    private String buildUrl(String path, Object... uriVariables) {
        return UriComponentsBuilder.fromUriString("http://localhost:" + port + path).buildAndExpand(uriVariables).toUriString();
    }
}
