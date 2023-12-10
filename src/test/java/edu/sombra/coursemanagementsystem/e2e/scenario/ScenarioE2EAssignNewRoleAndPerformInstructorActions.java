package edu.sombra.coursemanagementsystem.e2e.scenario;

import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.dto.auth.RegisterDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseActionDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentApplyForCourseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentResponseDTO;
import edu.sombra.coursemanagementsystem.dto.file.FileResponseDTO;
import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.LessonResponseDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserResponseDTO;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ScenarioE2EAssignNewRoleAndPerformInstructorActions {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final HttpHeaders adminHeaders = new HttpHeaders();
    private static final HttpHeaders studentHeaders = new HttpHeaders();
    private static final HttpHeaders instructorHeaders = new HttpHeaders();

    @Test
    void test_PerformInstructorScenario() {
        //Register user without role
        RegisterDTO registerUserDTO = RegisterDTO.builder()
                .firstName("Steve")
                .lastName("Peterson")
                .email("steve@gmail.com")
                .password("123")
                .build();
        registerUser(registerUserDTO);

        RegisterDTO registerStudentDTO = RegisterDTO.builder()
                .firstName("Ann")
                .lastName("Vysotska")
                .email("ann@gmail.com")
                .password("123")
                .build();
        registerUser(registerStudentDTO);

        //Authenticate as admin
        String adminJwtToken = authenticate("admin@gmail.com", "adminPAss");

        //Assign new role to user
        assignRoleToCreatedUser(adminJwtToken);

        //Create new course
        CourseResponseDTO createdCourse = createCourse(adminJwtToken);

        //Find user by email
        UserResponseDTO foundStudentResponse = findUserByEmail(registerStudentDTO);

        //Assign user to course
        EnrollmentApplyForCourseDTO applyForCourseDTO = EnrollmentApplyForCourseDTO.builder()
                .courseName(createdCourse.getCourseName())
                .userId(foundStudentResponse.getId())
                .build();

        EnrollmentResponseDTO enrollmentResponseDTO = assignUserToCourse(applyForCourseDTO);

        //Start course
        startCourse(createdCourse.getCourseId());

        //Authenticate as student
        String studentJwtToken = authenticate("ann@gmail.com", "123");

        //Find all lessons in course
        List<LessonResponseDTO> lessonsInCourse = findAllLessonsByCourseId(enrollmentResponseDTO, studentJwtToken);

        //find all homework by studentId and courseId
        GetHomeworkDTO homeworkDTO = findHomeworkByStudentAndCourse(lessonsInCourse.get(0).getId(), foundStudentResponse.getId(), studentJwtToken);

        //Upload homework
        FileResponseDTO uploadedHomework = uploadHomework(studentJwtToken, foundStudentResponse, homeworkDTO);


        //Authenticate as instructor
        String instructorJwtToken = authenticate("steve@gmail.com", "123");
    }

    private FileResponseDTO uploadHomework(String studentJwtToken, UserResponseDTO foundStudentResponse, GetHomeworkDTO homeworkDTO) {
        studentHeaders.setBearerAuth(studentJwtToken);
        studentHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource("output.csv"));
        body.add("userId", foundStudentResponse.getId());
        body.add("lessonId", homeworkDTO.getLesson().getId());

        HttpEntity<MultiValueMap<String, Object>> uploadHomeworkRequestEntity = new HttpEntity<>(body, studentHeaders);

        ResponseEntity<FileResponseDTO> uploadHomeworkResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/files/upload"),
                HttpMethod.POST,
                uploadHomeworkRequestEntity,
                FileResponseDTO.class
        );
        assertEquals(HttpStatus.OK, uploadHomeworkResponseEntity.getStatusCode());
        return uploadHomeworkResponseEntity.getBody();
    }

    private GetHomeworkDTO findHomeworkByStudentAndCourse(Long lessonId, Long studentId, String bearerToken) {
        studentHeaders.setBearerAuth(bearerToken);
        studentHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        ResponseEntity<GetHomeworkDTO> homeworkByStudentAndCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/homework/{lessonId}/{userId}", lessonId, studentId),
                HttpMethod.GET,
                new HttpEntity<>(studentHeaders),
                GetHomeworkDTO.class
        );
        assertEquals(HttpStatus.OK, homeworkByStudentAndCourseResponse.getStatusCode());
        assertNotNull(homeworkByStudentAndCourseResponse.getBody());
        return homeworkByStudentAndCourseResponse.getBody();
    }

    private List<LessonResponseDTO> findAllLessonsByCourseId(EnrollmentResponseDTO enrollmentResponseDTO, String bearerToken) {
        studentHeaders.setBearerAuth(bearerToken);
        studentHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<List<LessonResponseDTO>> lessonsInCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/lesson/find-all/{id}", enrollmentResponseDTO.getCourseId()),
                HttpMethod.GET,
                new HttpEntity<>(studentHeaders),
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(HttpStatus.OK, lessonsInCourseResponse.getStatusCode());
        assertNotNull(lessonsInCourseResponse.getBody());
        return lessonsInCourseResponse.getBody();
    }

    private void startCourse(Long courseId) {
        CourseActionDTO startCourseDTO = CourseActionDTO.builder()
                .courseId(courseId)
                .action("start")
                .build();

        HttpEntity<CourseActionDTO> startCourseRequestEntity = new HttpEntity<>(startCourseDTO, adminHeaders);

        ResponseEntity<CourseResponseDTO> startCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/course"),
                HttpMethod.PUT,
                startCourseRequestEntity,
                CourseResponseDTO.class
        );
        assertEquals(HttpStatus.OK, startCourseResponse.getStatusCode());
        assertNotNull(startCourseResponse.getBody());
    }

    private EnrollmentResponseDTO assignUserToCourse(EnrollmentApplyForCourseDTO applyForCourseDTO) {
        HttpEntity<EnrollmentApplyForCourseDTO> assignUserToCourseRequestEntity = new HttpEntity<>(applyForCourseDTO, adminHeaders);

        ResponseEntity<EnrollmentResponseDTO> assignUserToCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/enrollment/user/apply"),
                HttpMethod.POST,
                assignUserToCourseRequestEntity,
                EnrollmentResponseDTO.class
        );
        assertEquals(HttpStatus.OK, assignUserToCourseResponse.getStatusCode());
        assertEquals(applyForCourseDTO.getCourseName(), assignUserToCourseResponse.getBody().getCourseName());
        assertNotNull(assignUserToCourseResponse.getBody());
        return assignUserToCourseResponse.getBody();
    }

    private UserResponseDTO findUserByEmail(RegisterDTO registerStudentDTO) {
        ResponseEntity<UserResponseDTO> applyForCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/user/email/{email}", registerStudentDTO.getEmail()),
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                UserResponseDTO.class
        );
        assertEquals(HttpStatus.OK, applyForCourseResponse.getStatusCode());
        assertNotNull(applyForCourseResponse.getBody());
        return applyForCourseResponse.getBody();
    }

    private CourseResponseDTO createCourse(String adminJwtToken) {
        CourseDTO createCourseDTO = CourseDTO.builder()
                .name("Spark Intro")
                .startDate(LocalDate.now())
                .status(CourseStatus.WAIT)
                .started(false)
                .instructorEmail("steve@gmail.com")
                .numberOfLessons(10L)
                .build();

        adminHeaders.setBearerAuth(adminJwtToken);

        HttpEntity<CourseDTO> createCourseRequestEntity = new HttpEntity<>(createCourseDTO, adminHeaders);

        ResponseEntity<CourseResponseDTO> createdCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/course/create"),
                HttpMethod.POST,
                createCourseRequestEntity,
                CourseResponseDTO.class
        );
        assertEquals(HttpStatus.OK, createdCourseResponse.getStatusCode());
        assertEquals(createCourseDTO.getName(), createdCourseResponse.getBody().getCourseName());
        assertNotNull(createdCourseResponse.getBody());
        return createdCourseResponse.getBody();
    }

    private void assignRoleToCreatedUser(String adminJwtToken) {
        UserDTO userDTO = UserDTO.builder()
                .role(RoleEnum.INSTRUCTOR)
                .email("steve@gmail.com")
                .build();

        adminHeaders.setBearerAuth(adminJwtToken);

        HttpEntity<UserDTO> downloadFileRequestEntity = new HttpEntity<>(userDTO, adminHeaders);

        ResponseEntity<UserResponseDTO> assignNewRoleResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/user/assign-role"),
                HttpMethod.POST,
                downloadFileRequestEntity,
                UserResponseDTO.class
        );

        assertEquals(HttpStatus.OK, assignNewRoleResponseEntity.getStatusCode());
        assertEquals(userDTO.getEmail(), assignNewRoleResponseEntity.getBody().getEmail());
    }

    private String authenticate(String email, String password) {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO(email, password);
        ResponseEntity<AuthenticationResponse> authenticationResponseEntity = restTemplate.postForEntity(
                "/api/v1/auth/authenticate",
                authenticationDTO,
                AuthenticationResponse.class
        );

        assertEquals(HttpStatus.OK, authenticationResponseEntity.getStatusCode());
        AuthenticationResponse authenticationResponse = authenticationResponseEntity.getBody();
        return Objects.requireNonNull(authenticationResponse).getAccessToken();
    }

    private void registerUser(RegisterDTO registerDTO) {
        ResponseEntity<AuthenticationResponse> registerUserResponseEntity = restTemplate.postForEntity(
                "/api/v1/auth/register",
                registerDTO,
                AuthenticationResponse.class
        );
        assertEquals(HttpStatus.OK, registerUserResponseEntity.getStatusCode());
    }

    private String buildUrl(String path, Object... uriVariables) {
        return UriComponentsBuilder.fromUriString("http://localhost:" + port + path).buildAndExpand(uriVariables).toUriString();
    }
}
