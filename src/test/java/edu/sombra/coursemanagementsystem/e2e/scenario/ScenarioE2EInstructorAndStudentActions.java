package edu.sombra.coursemanagementsystem.e2e.scenario;

import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.dto.auth.RegisterDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseActionDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseAssignedToUserDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.course.LessonsByCourseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentApplyForCourseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentResponseDTO;
import edu.sombra.coursemanagementsystem.dto.feedback.CourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.dto.feedback.GetCourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.dto.file.FileResponseDTO;
import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkByLessonDTO;
import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkDTO;
import edu.sombra.coursemanagementsystem.dto.homework.HomeworkDTO;
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
class ScenarioE2EInstructorAndStudentActions {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final HttpHeaders adminHeaders = new HttpHeaders();
    private static final HttpHeaders studentHeaders = new HttpHeaders();
    private static final HttpHeaders instructorHeaders = new HttpHeaders();

    @Test
    void test_PerformInstructorAndUserScenario() {
        //Register users without role
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
        GetHomeworkDTO homework1DTO = findHomeworkByStudentAndLesson(lessonsInCourse.get(0).getId(), foundStudentResponse.getId(), studentJwtToken);
        GetHomeworkDTO homework2DTO = findHomeworkByStudentAndLesson(lessonsInCourse.get(1).getId(), foundStudentResponse.getId(), studentJwtToken);
        GetHomeworkDTO homework3DTO = findHomeworkByStudentAndLesson(lessonsInCourse.get(2).getId(), foundStudentResponse.getId(), studentJwtToken);
        GetHomeworkDTO homework4DTO = findHomeworkByStudentAndLesson(lessonsInCourse.get(3).getId(), foundStudentResponse.getId(), studentJwtToken);
        GetHomeworkDTO homework5DTO = findHomeworkByStudentAndLesson(lessonsInCourse.get(4).getId(), foundStudentResponse.getId(), studentJwtToken);

        //Upload homework
        FileResponseDTO uploadedHomework1 = uploadHomework(studentJwtToken, foundStudentResponse, homework1DTO);
        FileResponseDTO uploadedHomework2 = uploadHomework(studentJwtToken, foundStudentResponse, homework2DTO);

        //Authenticate as instructor
        String instructorJwtToken = authenticate("steve@gmail.com", "123");

        //Download homework
        downloadHomework(instructorJwtToken, uploadedHomework1);
        downloadHomework(instructorJwtToken, uploadedHomework2);

        //Set mark for the homework
        setMarkForHomework(foundStudentResponse, instructorJwtToken, homework1DTO, 85L);
        setMarkForHomework(foundStudentResponse, instructorJwtToken, homework2DTO, 65L);

        //Get total marks
        LessonsByCourseDTO lessonsWithTotalMarks = getLessonsWithTotalMarks(foundStudentResponse, createdCourse);

        //check is user passed course
        assertFalse(lessonsWithTotalMarks.isPassed());

        //user passed all homeworks
        FileResponseDTO uploadedHomework3 = uploadHomework(studentJwtToken, foundStudentResponse, homework3DTO);
        FileResponseDTO uploadedHomework4 = uploadHomework(studentJwtToken, foundStudentResponse, homework4DTO);
        FileResponseDTO uploadedHomework5 = uploadHomework(studentJwtToken, foundStudentResponse, homework5DTO);

        //Instructor set marks for each homework
        downloadHomework(instructorJwtToken, uploadedHomework3);
        downloadHomework(instructorJwtToken, uploadedHomework4);
        downloadHomework(instructorJwtToken, uploadedHomework5);

        setMarkForHomework(foundStudentResponse, instructorJwtToken, homework3DTO, 95L);
        setMarkForHomework(foundStudentResponse, instructorJwtToken, homework4DTO, 80L);
        setMarkForHomework(foundStudentResponse, instructorJwtToken, homework5DTO, 75L);

        //Get total marks
        LessonsByCourseDTO allLessonsWithTotalMarks = getLessonsWithTotalMarks(foundStudentResponse, createdCourse);

        //Course should be successfully passed
        assertTrue(allLessonsWithTotalMarks.isPassed());

        //Set feedback for course
        addFeedbackForCourse(createdCourse, foundStudentResponse);

        //Logout
        logoutUserUsingHeader(adminHeaders);
        //Logout
        logoutUserUsingHeader(studentHeaders);
        //Logout
        logoutUserUsingHeader(instructorHeaders);
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

    private void addFeedbackForCourse(CourseResponseDTO createdCourse, UserResponseDTO foundStudentResponse) {
        CourseFeedbackDTO feedbackDTO = new CourseFeedbackDTO();
        feedbackDTO.setCourseId(createdCourse.getCourseId());
        feedbackDTO.setStudentId(foundStudentResponse.getId());
        feedbackDTO.setFeedbackText("Looks good, thanks!");

        HttpEntity<CourseFeedbackDTO> addFeedbackRequestEntity = new HttpEntity<>(feedbackDTO, instructorHeaders);

        ResponseEntity<GetCourseFeedbackDTO> addFeedbackResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/feedback"),
                HttpMethod.POST,
                addFeedbackRequestEntity,
                GetCourseFeedbackDTO.class
        );

        assertEquals(feedbackDTO.getFeedbackText(), Objects.requireNonNull(addFeedbackResponseEntity.getBody()).getFeedbackText());
    }

    private LessonsByCourseDTO getLessonsWithTotalMarks(UserResponseDTO foundStudentResponse, CourseResponseDTO createdCourse) {
        CourseAssignedToUserDTO dto = CourseAssignedToUserDTO.builder()
                .courseId(createdCourse.getCourseId())
                .userId(foundStudentResponse.getId())
                .build();
        HttpEntity<CourseAssignedToUserDTO> studentsOnCoursesRequestEntity = new HttpEntity<>(dto, instructorHeaders);
        ResponseEntity<LessonsByCourseDTO> getListOfLessonsOnCourseResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/course/student/lessons"),
                HttpMethod.POST,
                studentsOnCoursesRequestEntity,
                LessonsByCourseDTO.class
        );

        assertEquals(HttpStatus.OK, getListOfLessonsOnCourseResponseEntity.getStatusCode());
        assertNotNull(getListOfLessonsOnCourseResponseEntity.getBody());
        return getListOfLessonsOnCourseResponseEntity.getBody();
    }

    private void downloadHomework(String instructorJwtToken, FileResponseDTO uploadedHomework) {
        instructorHeaders.setBearerAuth(instructorJwtToken);

        HttpEntity<String> downloadFileRequestEntity = new HttpEntity<>(instructorHeaders);

        ResponseEntity<Resource> downloadFileResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/files/download/{lessonId}", uploadedHomework.getId()),
                HttpMethod.GET,
                downloadFileRequestEntity,
                Resource.class
        );
        assertEquals(HttpStatus.OK, downloadFileResponseEntity.getStatusCode());
        assertEquals(uploadedHomework.getName(), Objects.requireNonNull(downloadFileResponseEntity.getBody()).getFilename());
    }

    private GetHomeworkDTO setMarkForHomework(UserResponseDTO foundStudentResponse, String instructorJwtToken,
                                              GetHomeworkDTO existedHomework, Long mark) {
        HomeworkDTO homeworkDTO = new HomeworkDTO();
        homeworkDTO.setUserId(foundStudentResponse.getId());
        homeworkDTO.setHomeworkId(existedHomework.getId());
        homeworkDTO.setMark(mark);

        instructorHeaders.setBearerAuth(instructorJwtToken);
        HttpEntity<HomeworkDTO> setMarkRequestEntity = new HttpEntity<>(homeworkDTO, instructorHeaders);

        ResponseEntity<GetHomeworkDTO> setMarkResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/homework/mark"),
                HttpMethod.PUT,
                setMarkRequestEntity,
                GetHomeworkDTO.class
        );
        assertEquals(homeworkDTO.getMark(), setMarkResponseEntity.getBody().getMark());
        assertEquals(homeworkDTO.getUserId(), setMarkResponseEntity.getBody().getUserId());
        return setMarkResponseEntity.getBody();
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

    private GetHomeworkDTO findHomeworkByStudentAndLesson(Long lessonId, Long studentId, String bearerToken) {
        GetHomeworkByLessonDTO getHomeworkByLessonDTO = GetHomeworkByLessonDTO.builder()
                .lessonId(lessonId)
                .build();

        studentHeaders.setBearerAuth(bearerToken);
        studentHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        ResponseEntity<GetHomeworkDTO> homeworkByStudentAndCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/homework"),
                HttpMethod.POST,
                new HttpEntity<>(getHomeworkByLessonDTO, studentHeaders),
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
        UserDTO userDTO = UserDTO.builder()
                .email(registerStudentDTO.getEmail())
                .build();
        ResponseEntity<UserResponseDTO> applyForCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/user/email"),
                HttpMethod.POST,
                new HttpEntity<>(userDTO, adminHeaders),
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
                .instructorEmail("steve@gmail.com")
                .numberOfLessons(5L)
                .build();

        adminHeaders.setBearerAuth(adminJwtToken);

        HttpEntity<CourseDTO> createCourseRequestEntity = new HttpEntity<>(createCourseDTO, adminHeaders);

        ResponseEntity<CourseResponseDTO> createdCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/course/create"),
                HttpMethod.POST,
                createCourseRequestEntity,
                CourseResponseDTO.class
        );
        assertEquals(HttpStatus.CREATED, createdCourseResponse.getStatusCode());
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
        assertEquals(HttpStatus.CREATED, registerUserResponseEntity.getStatusCode());
    }

    private String buildUrl(String path, Object... uriVariables) {
        return UriComponentsBuilder.fromUriString("http://localhost:" + port + path).buildAndExpand(uriVariables).toUriString();
    }
}
