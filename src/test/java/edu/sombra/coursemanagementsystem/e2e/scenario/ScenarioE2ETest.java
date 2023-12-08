package edu.sombra.coursemanagementsystem.e2e.scenario;

import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.dto.course.CourseActionDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.course.LessonsByCourseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentApplyForCourseDTO;
import edu.sombra.coursemanagementsystem.dto.feedback.CourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.dto.homework.HomeworkDTO;
import edu.sombra.coursemanagementsystem.dto.user.CreateUserDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserResponseDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ScenarioE2ETest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void test_CreateStudentAndAssignToTheCourse() {
        //login as Admin
        AuthenticationDTO authenticationDTO = new AuthenticationDTO("admin@gmail.com", "adminPAss");

        ResponseEntity<AuthenticationResponse> responseEntity = restTemplate.postForEntity(
                "/api/v1/auth/authenticate",
                authenticationDTO,
                AuthenticationResponse.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        AuthenticationResponse authenticationResponse = responseEntity.getBody();
        String adminJwtToken = Objects.requireNonNull(authenticationResponse).getAccessToken();

        //Create student
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setLastName("testUser");
        createUserDTO.setFirstName("testUser");
        createUserDTO.setEmail("teststudent@example.com");
        createUserDTO.setPassword("studentPass");
        createUserDTO.setRole(RoleEnum.STUDENT);

        HttpHeaders adminHeader = new HttpHeaders();
        adminHeader.setBearerAuth(adminJwtToken);

        HttpEntity<CreateUserDTO> requestEntity = new HttpEntity<>(createUserDTO, adminHeader);

        ResponseEntity<UserResponseDTO> createdUserResponse = restTemplate.exchange(
                buildUrl("/api/v1/user/create"),
                HttpMethod.POST,
                requestEntity,
                UserResponseDTO.class
        );

        assertEquals(HttpStatus.OK, createdUserResponse.getStatusCode());
        assertNotNull(createdUserResponse.getBody());

        /*
         Create course
         */
        CourseDTO createCourseDTO = CourseDTO.builder()
                .name("Python learn")
                .startDate(LocalDate.now())
                .status(CourseStatus.WAIT)
                .started(false)
                .instructorEmail("instructor@gmail.com")
                .numberOfLessons(10L)
                .build();

        HttpEntity<CourseDTO> createCourseRequestEntity = new HttpEntity<>(createCourseDTO, adminHeader);

        ResponseEntity<Course> createdCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/course/create"),
                HttpMethod.POST,
                createCourseRequestEntity,
                Course.class
        );

        assertEquals(HttpStatus.OK, createdCourseResponse.getStatusCode());
        assertNotNull(createdCourseResponse.getBody());

/*        //Logout
        restTemplate.exchange(
                buildUrl("/api/v1/auth/logout"),
                HttpMethod.GET,
                new HttpEntity(adminHeader),
                Void.class
        );*/

        /*
         login as student
         */
        AuthenticationDTO studentAuthenticationDTO = new AuthenticationDTO("teststudent@example.com", "studentPass");

        ResponseEntity<AuthenticationResponse> studentResponseEntity = restTemplate.postForEntity(
                "/api/v1/auth/authenticate",
                studentAuthenticationDTO,
                AuthenticationResponse.class
        );

        assertEquals(HttpStatus.OK, studentResponseEntity.getStatusCode());
        AuthenticationResponse studentAuthenticationResponse = studentResponseEntity.getBody();
        String studentJwtToken = Objects.requireNonNull(studentAuthenticationResponse).getAccessToken();


        //Apply student to course
        HttpHeaders studentHeader = new HttpHeaders();
        studentHeader.setBearerAuth(studentJwtToken);

        EnrollmentApplyForCourseDTO enrollmentDTO = EnrollmentApplyForCourseDTO.builder()
                .courseName("Course A")
                .build();

        HttpEntity<EnrollmentApplyForCourseDTO> studentRequestEntity = new HttpEntity<>(enrollmentDTO, studentHeader);

        ResponseEntity<String> applyToCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/enrollment/user/apply"),
                HttpMethod.POST,
                studentRequestEntity,
                String.class
        );

        assertEquals(HttpStatus.OK, applyToCourseResponse.getStatusCode());
        assertNotNull(applyToCourseResponse.getBody());

        //start course
        CourseActionDTO startCourseDTO = CourseActionDTO.builder()
                .courseId(4L)
                .action("start")
                .build();

        HttpEntity<CourseActionDTO> startCourseRequestEntity = new HttpEntity<>(startCourseDTO, studentHeader);

        ResponseEntity<CourseResponseDTO> startCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/course"),
                HttpMethod.PUT,
                startCourseRequestEntity,
                CourseResponseDTO.class
        );

        assertEquals(HttpStatus.OK, startCourseResponse.getStatusCode());
        assertNotNull(startCourseResponse.getBody());

        //Upload homework file

        HttpHeaders uploadHomeworkHeaders = new HttpHeaders();
        uploadHomeworkHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        uploadHomeworkHeaders.setBearerAuth(studentJwtToken);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource("output.csv"));
        body.add("userId", "8");
        body.add("lessonId", "1");

        HttpEntity<MultiValueMap<String, Object>> uploadHomeworkRequestEntity = new HttpEntity<>(body, uploadHomeworkHeaders);

        ResponseEntity<String> uploadHomeworkResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/files/upload"),
                HttpMethod.POST,
                uploadHomeworkRequestEntity,
                String.class
        );

        assertEquals(HttpStatus.OK, uploadHomeworkResponseEntity.getStatusCode());
        assertEquals("File uploaded successfully", uploadHomeworkResponseEntity.getBody());

        //Login as instructor
        AuthenticationDTO instructorAuthenticationDTO = new AuthenticationDTO("instructor@gmail.com", "instructorPass");

        ResponseEntity<AuthenticationResponse> instructorResponseEntity = restTemplate.postForEntity(
                "/api/v1/auth/authenticate",
                instructorAuthenticationDTO,
                AuthenticationResponse.class
        );

        assertEquals(HttpStatus.OK, instructorResponseEntity.getStatusCode());
        AuthenticationResponse instructorAuthenticationResponse = instructorResponseEntity.getBody();
        String instructorJwtToken = Objects.requireNonNull(instructorAuthenticationResponse).getAccessToken();

        //Download homework
        HttpHeaders instructorHeaders = new HttpHeaders();
        instructorHeaders.setBearerAuth(instructorJwtToken);

        HttpEntity<String> downloadFileRequestEntity = new HttpEntity<>(instructorHeaders);

        ResponseEntity<Resource> downloadFileResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/files/download/{lessonId}", 3),
                HttpMethod.GET,
                downloadFileRequestEntity,
                Resource.class
        );

        assertEquals(HttpStatus.OK, downloadFileResponseEntity.getStatusCode());
        assertEquals("output.csv", Objects.requireNonNull(downloadFileResponseEntity.getBody()).getFilename());

        //Set mark for homework
        HomeworkDTO homeworkDTO = new HomeworkDTO();
        homeworkDTO.setUserId(8L);
        //homeworkDTO.setHomeworkId(3L);
        homeworkDTO.setHomeworkId(4L);
        homeworkDTO.setMark(90L);

        HttpEntity<HomeworkDTO> setMarkRequestEntity = new HttpEntity<>(homeworkDTO, instructorHeaders);

        ResponseEntity<String> setMarkResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/homework/mark"),
                HttpMethod.PUT,
                setMarkRequestEntity,
                String.class
        );

        assertEquals("Mark saved successfully", setMarkResponseEntity.getBody());

        //Add feedback
        CourseFeedbackDTO feedbackDTO = new CourseFeedbackDTO();
        feedbackDTO.setCourseId(3L);
        feedbackDTO.setStudentId(4L);
        feedbackDTO.setFeedbackText("Great result!");

        HttpEntity<CourseFeedbackDTO> addFeedbackRequestEntity = new HttpEntity<>(feedbackDTO, instructorHeaders);

        ResponseEntity<String> addFeedbackResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/feedback"),
                HttpMethod.POST,
                addFeedbackRequestEntity,
                String.class
        );

        assertEquals("Feedback saved successfully", addFeedbackResponseEntity.getBody());

        //Get list of instructor courses
        HttpEntity<Void> instructorCoursesRequestEntity = new HttpEntity<>(instructorHeaders);

        ResponseEntity<List<CourseResponseDTO>> getInstructorCoursesResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/course/instructor/{instructorId}", 3),
                HttpMethod.GET,
                instructorCoursesRequestEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, getInstructorCoursesResponseEntity.getStatusCode());
        assertNotNull(getInstructorCoursesResponseEntity.getBody());

        //Get list of students on the course
        HttpEntity<Void> studentsOnCoursesRequestEntity = new HttpEntity<>(instructorHeaders);

        ResponseEntity<List<CourseResponseDTO>> getStudentsOnCourseResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/course/instructor/{instructorId}/{courseId}", 3, 3),
                HttpMethod.GET,
                studentsOnCoursesRequestEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, getStudentsOnCourseResponseEntity.getStatusCode());
        assertNotNull(getStudentsOnCourseResponseEntity.getBody());

        //Get list of student courses
        ResponseEntity<List<CourseResponseDTO>> getListOfStudentsCoursesResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/course/student/{studentId}", 4),
                HttpMethod.GET,
                studentsOnCoursesRequestEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, getListOfStudentsCoursesResponseEntity.getStatusCode());
        assertNotNull(getListOfStudentsCoursesResponseEntity.getBody());

        //Get list of lessons on the course
        ResponseEntity<LessonsByCourseDTO> getListOfLessonsOnCourseResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/course/student/lessons/{studentId}/{courseId}", 8, 1),
                HttpMethod.GET,
                studentsOnCoursesRequestEntity,
                LessonsByCourseDTO.class
        );

        assertEquals(HttpStatus.OK, getListOfLessonsOnCourseResponseEntity.getStatusCode());
        assertNotNull(getListOfLessonsOnCourseResponseEntity.getBody());
    }

    private String buildUrl(String path, Object... uriVariables) {
        return UriComponentsBuilder.fromUriString("http://localhost:" + port + path).buildAndExpand(uriVariables).toUriString();
    }
}