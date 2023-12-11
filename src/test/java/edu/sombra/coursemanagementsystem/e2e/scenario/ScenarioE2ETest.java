package edu.sombra.coursemanagementsystem.e2e.scenario;

import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.dto.course.CourseActionDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.course.LessonsByCourseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentApplyForCourseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentResponseDTO;
import edu.sombra.coursemanagementsystem.dto.feedback.CourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.dto.feedback.GetCourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.dto.file.FileResponseDTO;
import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkDTO;
import edu.sombra.coursemanagementsystem.dto.homework.HomeworkDTO;
import edu.sombra.coursemanagementsystem.dto.user.CreateUserDTO;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ScenarioE2ETest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final HttpHeaders adminHeader = new HttpHeaders();


    @Test
    void test_CreateStudentAndAssignToTheCourse() {
        //login as Admin
        String adminJwtToken = authenticateAndGetJwtToken("admin@gmail.com", "adminPAss");

        //Create student
        UserResponseDTO createdUserResponse = createStudentWithAdminToken(adminJwtToken);

        /*
         Create course
         */
        ResponseEntity<CourseResponseDTO> createdCourseResponse = createCourseAndReturnCourseResponseDTO();

        /*
         login as student
         */
        String studentJwtToken = authenticateAndGetJwtToken("teststudent@example.com", "studentPass");


        //Apply student to course
        HttpHeaders studentHeader = applyForCourseWithStudentToken(studentJwtToken);

        //start course
        startCourse(createdCourseResponse);

        //Upload homework file
        uploadHomeworkWithStudentToken(studentJwtToken, createdUserResponse);

        //Login as instructor
        String instructorJwtToken = authenticateAndGetJwtToken("instructor@gmail.com", "instructorPass");

        //Download homework
        HttpHeaders instructorHeaders = downloadFileAndGetInstructorHeaders(instructorJwtToken);

        //Set mark for homework
        setMarkForHomework(createdUserResponse, instructorHeaders);

        //Add feedback
        addCourseFeedback(instructorHeaders);

        //Get list of instructor courses
        getInstructorCourses(instructorHeaders);

        //Get list of students on the course
        HttpEntity<Void> studentsOnCoursesRequestEntity = getStudentsOnCourse(instructorHeaders);

        //Get list of student courses
        getListOfStudentCourses(studentsOnCoursesRequestEntity);

        //TODO: change all request data and get data from another dtos
        //Get list of lessons on the course
        getListOfLessonsOnCourse(studentsOnCoursesRequestEntity);

        //Admin Logout
        logoutUserUsingHeader(adminHeader);
        //Instructor logout
        logoutUserUsingHeader(instructorHeaders);
        //Student logout
        logoutUserUsingHeader(studentHeader);
    }

    private void getListOfLessonsOnCourse(HttpEntity<Void> studentsOnCoursesRequestEntity) {
        ResponseEntity<LessonsByCourseDTO> getListOfLessonsOnCourseResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/course/student/lessons/{studentId}/{courseId}", 4, 1),
                HttpMethod.GET,
                studentsOnCoursesRequestEntity,
                LessonsByCourseDTO.class
        );

        assertEquals(HttpStatus.OK, getListOfLessonsOnCourseResponseEntity.getStatusCode());
        assertNotNull(getListOfLessonsOnCourseResponseEntity.getBody());
    }

    private void getListOfStudentCourses(HttpEntity<Void> studentsOnCoursesRequestEntity) {
        ResponseEntity<List<CourseResponseDTO>> getListOfStudentsCoursesResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/course/student/{studentId}", 4),
                HttpMethod.GET,
                studentsOnCoursesRequestEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, getListOfStudentsCoursesResponseEntity.getStatusCode());
        assertNotNull(getListOfStudentsCoursesResponseEntity.getBody());
    }

    private HttpEntity<Void> getStudentsOnCourse(HttpHeaders instructorHeaders) {
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
        return studentsOnCoursesRequestEntity;
    }

    private void getInstructorCourses(HttpHeaders instructorHeaders) {
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
    }

    private void addCourseFeedback(HttpHeaders instructorHeaders) {
        CourseFeedbackDTO feedbackDTO = new CourseFeedbackDTO();
        feedbackDTO.setCourseId(3L);
        feedbackDTO.setStudentId(4L);
        feedbackDTO.setFeedbackText("Great result!");

        HttpEntity<CourseFeedbackDTO> addFeedbackRequestEntity = new HttpEntity<>(feedbackDTO, instructorHeaders);

        ResponseEntity<GetCourseFeedbackDTO> addFeedbackResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/feedback"),
                HttpMethod.POST,
                addFeedbackRequestEntity,
                GetCourseFeedbackDTO.class
        );

        assertEquals(feedbackDTO.getFeedbackText(), Objects.requireNonNull(addFeedbackResponseEntity.getBody()).getFeedbackText());
    }

    private void setMarkForHomework(UserResponseDTO createdUserResponse, HttpHeaders instructorHeaders) {
        HomeworkDTO homeworkDTO = new HomeworkDTO();
        homeworkDTO.setUserId(createdUserResponse.getId());
        homeworkDTO.setHomeworkId(18L);
        homeworkDTO.setMark(90L);

        HttpEntity<HomeworkDTO> setMarkRequestEntity = new HttpEntity<>(homeworkDTO, instructorHeaders);

        ResponseEntity<GetHomeworkDTO> setMarkResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/homework/mark"),
                HttpMethod.PUT,
                setMarkRequestEntity,
                GetHomeworkDTO.class
        );
        assertEquals(homeworkDTO.getMark(), setMarkResponseEntity.getBody().getMark());
        assertEquals(homeworkDTO.getUserId(), setMarkResponseEntity.getBody().getUserId());
    }

    private HttpHeaders downloadFileAndGetInstructorHeaders(String instructorJwtToken) {
        HttpHeaders instructorHeaders = new HttpHeaders();
        instructorHeaders.setBearerAuth(instructorJwtToken);

        HttpEntity<String> downloadFileRequestEntity = new HttpEntity<>(instructorHeaders);

        ResponseEntity<Resource> downloadFileResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/files/download/{lessonId}", 1),
                HttpMethod.GET,
                downloadFileRequestEntity,
                Resource.class
        );
        assertEquals(HttpStatus.OK, downloadFileResponseEntity.getStatusCode());
        assertEquals("file1.txt", Objects.requireNonNull(downloadFileResponseEntity.getBody()).getFilename());
        return instructorHeaders;
    }

    private void uploadHomeworkWithStudentToken(String studentJwtToken, UserResponseDTO createdUserResponse) {
        HttpHeaders uploadHomeworkHeaders = new HttpHeaders();
        uploadHomeworkHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        uploadHomeworkHeaders.setBearerAuth(studentJwtToken);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource("output.csv"));
        body.add("userId", createdUserResponse.getId());
        body.add("lessonId", "1");

        HttpEntity<MultiValueMap<String, Object>> uploadHomeworkRequestEntity = new HttpEntity<>(body, uploadHomeworkHeaders);

        ResponseEntity<FileResponseDTO> uploadHomeworkResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/files/upload"),
                HttpMethod.POST,
                uploadHomeworkRequestEntity,
                FileResponseDTO.class
        );
        assertEquals(HttpStatus.OK, uploadHomeworkResponseEntity.getStatusCode());
    }

    private void startCourse(ResponseEntity<CourseResponseDTO> createdCourseResponse) {
        CourseActionDTO startCourseDTO = CourseActionDTO.builder()
                .courseId(createdCourseResponse.getBody().getCourseId())
                .action("start")
                .build();

        HttpEntity<CourseActionDTO> startCourseRequestEntity = new HttpEntity<>(startCourseDTO, adminHeader);

        ResponseEntity<CourseResponseDTO> startCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/course"),
                HttpMethod.PUT,
                startCourseRequestEntity,
                CourseResponseDTO.class
        );

        assertEquals(HttpStatus.OK, startCourseResponse.getStatusCode());
        assertNotNull(startCourseResponse.getBody());
    }

    private HttpHeaders applyForCourseWithStudentToken(String studentJwtToken) {
        HttpHeaders studentHeader = new HttpHeaders();
        studentHeader.setBearerAuth(studentJwtToken);

        EnrollmentApplyForCourseDTO enrollmentDTO = EnrollmentApplyForCourseDTO.builder()
                .courseName("Course A")
                .build();

        HttpEntity<EnrollmentApplyForCourseDTO> studentRequestEntity = new HttpEntity<>(enrollmentDTO, studentHeader);

        ResponseEntity<EnrollmentResponseDTO> applyToCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/enrollment/user/apply"),
                HttpMethod.POST,
                studentRequestEntity,
                EnrollmentResponseDTO.class
        );

        assertEquals(HttpStatus.OK, applyToCourseResponse.getStatusCode());
        assertNotNull(applyToCourseResponse.getBody());
        return studentHeader;
    }

    private ResponseEntity<CourseResponseDTO> createCourseAndReturnCourseResponseDTO() {
        CourseDTO createCourseDTO = CourseDTO.builder()
                .name("Python learn")
                .startDate(LocalDate.now())
                .status(CourseStatus.WAIT)
                .started(false)
                .instructorEmail("instructor@gmail.com")
                .numberOfLessons(10L)
                .build();

        HttpEntity<CourseDTO> createCourseRequestEntity = new HttpEntity<>(createCourseDTO, adminHeader);

        ResponseEntity<CourseResponseDTO> createdCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/course/create"),
                HttpMethod.POST,
                createCourseRequestEntity,
                CourseResponseDTO.class
        );

        assertEquals(HttpStatus.OK, createdCourseResponse.getStatusCode());
        assertNotNull(createdCourseResponse.getBody());
        return createdCourseResponse;
    }

    private UserResponseDTO createStudentWithAdminToken(String adminJwtToken) {
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setLastName("testUser");
        createUserDTO.setFirstName("testUser");
        createUserDTO.setEmail("teststudent@example.com");
        createUserDTO.setPassword("studentPass");
        createUserDTO.setRole(RoleEnum.STUDENT);

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
        return createdUserResponse.getBody();
    }

    private String authenticateAndGetJwtToken(String mail, String adminPAss) {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO(mail, adminPAss);

        ResponseEntity<AuthenticationResponse> responseEntity = restTemplate.postForEntity(
                "/api/v1/auth/authenticate",
                authenticationDTO,
                AuthenticationResponse.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        AuthenticationResponse authenticationResponse = responseEntity.getBody();
        return Objects.requireNonNull(authenticationResponse).getAccessToken();
    }

    private void logoutUserUsingHeader(HttpHeaders headers) {
        ResponseEntity<Void> logout = restTemplate.exchange(
                buildUrl("/api/v1/auth/logout"),
                HttpMethod.GET,
                new HttpEntity(headers),
                Void.class
        );

        assertEquals(HttpStatus.OK, logout.getStatusCode());
        assertNull(logout.getBody());
    }

    private String buildUrl(String path, Object... uriVariables) {
        return UriComponentsBuilder.fromUriString("http://localhost:" + port + path).buildAndExpand(uriVariables).toUriString();
    }
}
