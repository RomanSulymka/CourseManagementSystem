package edu.sombra.coursemanagementsystem.e2e.scenario;

import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.dto.auth.RegisterDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.course.UpdateCourseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentApplyForCourseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentResponseDTO;
import edu.sombra.coursemanagementsystem.dto.feedback.CourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.dto.feedback.GetCourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.dto.file.FileResponseDTO;
import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkByLessonDTO;
import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkDTO;
import edu.sombra.coursemanagementsystem.dto.homework.HomeworkDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.LessonResponseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.UpdateLessonDTO;
import edu.sombra.coursemanagementsystem.dto.user.UpdateUserDTO;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ScenarioAdminE2ETest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    private static final HttpHeaders adminHeaders = new HttpHeaders();

    @Test
    void test_PerformAdminScenario() {
        //Register user without role
        RegisterDTO registerInstructorDTO = RegisterDTO.builder()
                .firstName("Steve")
                .lastName("Peterson")
                .email("steve@gmail.com")
                .password("123")
                .build();
        registerUser(registerInstructorDTO);

        RegisterDTO registerAdminDTO = RegisterDTO.builder()
                .firstName("Ann")
                .lastName("Vysotska")
                .email("ann@gmail.com")
                .password("123")
                .build();
        registerUser(registerAdminDTO);

        RegisterDTO registerStudentDTO = RegisterDTO.builder()
                .firstName("Peter")
                .lastName("Simon")
                .email("peter@gmail.com")
                .password("123")
                .build();
        registerUser(registerStudentDTO);

        //Authenticate as admin
        String adminJwtToken = authenticate("admin@gmail.com", "adminPAss");

        //Assign new roles
        assignRoleToCreatedUser(adminJwtToken, RoleEnum.INSTRUCTOR, registerInstructorDTO.getEmail());
        assignRoleToCreatedUser(adminJwtToken, RoleEnum.ADMIN, registerAdminDTO.getEmail());

        //Find user by email
        UserResponseDTO foundInstructorResponse = findUserByEmail(registerInstructorDTO);
        UserResponseDTO foundStudentResponse = findUserByEmail(registerStudentDTO);

        //Change user email
        changeUserEmail(foundInstructorResponse, adminJwtToken);

        //Create course
        CourseResponseDTO createdCourse = createCourse(adminJwtToken);

        //Assign student to course
        EnrollmentApplyForCourseDTO applyForCourseDTO = EnrollmentApplyForCourseDTO.builder()
                .courseName(createdCourse.getCourseName())
                .userId(foundStudentResponse.getId())
                .build();
        assignUserToCourse(applyForCourseDTO);

        //Change course name
        editCourseName(createdCourse, adminJwtToken);

        //Get all lessons by course
        List<LessonResponseDTO> lessonsResponse = findLessonsByCourse(createdCourse);

        //Change lesson name
        changeLessonName(lessonsResponse, adminJwtToken);

        //remove lesson from the course
        deleteLessonById(lessonsResponse);

        //find homeworks
        GetHomeworkDTO homework1DTO = findHomeworkByStudentAndCourse(lessonsResponse.get(0).getId(), foundStudentResponse.getId(), adminJwtToken);

        //upload homework
        uploadHomework(adminJwtToken, foundStudentResponse, homework1DTO);

        //Set mark
        setMarkForHomework(foundStudentResponse, adminJwtToken, homework1DTO, 85L);

        //set feedback
        addFeedbackForCourse(createdCourse, foundStudentResponse);

        //get all courses
        findAllCourses();

        //get all users
        findAllUsers();

        //logout
        logoutUserUsingHeader(adminHeaders);
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

    private List<UserResponseDTO> findAllUsers() {
        ResponseEntity<List<UserResponseDTO>> listUsersResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/user/find-all"),
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.OK, listUsersResponseEntity.getStatusCode());
        assertNotNull(listUsersResponseEntity.getBody());
        return listUsersResponseEntity.getBody();
    }

    private List<CourseResponseDTO> findAllCourses() {
        ResponseEntity<List<CourseResponseDTO>> listCoursesResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/course/find-all"),
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.OK, listCoursesResponseEntity.getStatusCode());
        assertNotNull(listCoursesResponseEntity.getBody());
        return listCoursesResponseEntity.getBody();
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

    private void deleteLessonById(List<LessonResponseDTO> lessonsResponse) {
        ResponseEntity<String> deleteLessonResponse = restTemplate.exchange(
                buildUrl("/api/v1/lesson/{id}", lessonsResponse.get(1).getId()),
                HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders),
                String.class
        );

        assertEquals(HttpStatus.OK, deleteLessonResponse.getStatusCode());
        assertEquals("Lesson deleted successfully", deleteLessonResponse.getBody());
    }

    private LessonResponseDTO changeLessonName(List<LessonResponseDTO> lessonsResponse, String adminJwtToken) {
        UpdateLessonDTO updatedLessonDTO = UpdateLessonDTO.builder()
                .id(lessonsResponse.get(0).getId())
                .name("How to setup JDK?")
                .courseId(lessonsResponse.get(0).getCourse().getCourseId())
                .build();

        adminHeaders.setBearerAuth(adminJwtToken);

        HttpEntity<UpdateLessonDTO> updateLessonRequestEntity = new HttpEntity<>(updatedLessonDTO, adminHeaders);

        ResponseEntity<LessonResponseDTO> updateLessonResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/lesson/edit"),
                HttpMethod.PUT,
                updateLessonRequestEntity,
                LessonResponseDTO.class
        );

        assertEquals(HttpStatus.OK, updateLessonResponseEntity.getStatusCode());
        assertEquals(updatedLessonDTO.getId(), updateLessonResponseEntity.getBody().getId());
        assertEquals(updatedLessonDTO.getName(), updateLessonResponseEntity.getBody().getName());
        return updateLessonResponseEntity.getBody();
    }

    private List<LessonResponseDTO> findLessonsByCourse(CourseResponseDTO createdCourse) {
        ResponseEntity<List<LessonResponseDTO>> applyForCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/lesson/find-all/{id}", createdCourse.getCourseId()),
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.OK, applyForCourseResponse.getStatusCode());
        assertNotNull(applyForCourseResponse.getBody());
        return applyForCourseResponse.getBody();
    }

    private void editCourseName(CourseResponseDTO createdCourse, String adminJwtToken) {
        UpdateCourseDTO updateCourseDTO = UpdateCourseDTO.builder()
                .id(createdCourse.getCourseId())
                .name("Software Engineering")
                .startDate(LocalDate.now())
                .build();

        adminHeaders.setBearerAuth(adminJwtToken);

        HttpEntity<UpdateCourseDTO> updateUserRequestEntity = new HttpEntity<>(updateCourseDTO, adminHeaders);

        ResponseEntity<CourseResponseDTO> updateUserResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/course/edit"),
                HttpMethod.PUT,
                updateUserRequestEntity,
                CourseResponseDTO.class
        );

        assertEquals(HttpStatus.OK, updateUserResponseEntity.getStatusCode());
        assertEquals(updateCourseDTO.getId(), updateUserResponseEntity.getBody().getCourseId());
        assertEquals(updateCourseDTO.getName(), updateUserResponseEntity.getBody().getCourseName());
    }

    private void changeUserEmail(UserResponseDTO foundInstructorResponse, String adminJwtToken) {
        UpdateUserDTO userDTO = UpdateUserDTO.builder()
                .id(foundInstructorResponse.getId())
                .firstName(foundInstructorResponse.getFirstName())
                .lastName(foundInstructorResponse.getLastName())
                .role(foundInstructorResponse.getRole())
                .email("steve-instructor@gmail.com")
                .build();

        adminHeaders.setBearerAuth(adminJwtToken);

        HttpEntity<UpdateUserDTO> updateUserRequestEntity = new HttpEntity<>(userDTO, adminHeaders);

        ResponseEntity<UserResponseDTO> updateUserResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/user/update"),
                HttpMethod.PUT,
                updateUserRequestEntity,
                UserResponseDTO.class
        );

        assertEquals(HttpStatus.OK, updateUserResponseEntity.getStatusCode());
        assertEquals(userDTO.getRole(), updateUserResponseEntity.getBody().getRole());
        assertEquals(userDTO.getEmail(), updateUserResponseEntity.getBody().getEmail());
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

    private void assignRoleToCreatedUser(String adminJwtToken, RoleEnum role, String email) {
        UserDTO userDTO = UserDTO.builder()
                .role(role)
                .email(email)
                .build();

        adminHeaders.setBearerAuth(adminJwtToken);

        HttpEntity<UserDTO> assignRoleRequestEntity = new HttpEntity<>(userDTO, adminHeaders);

        ResponseEntity<UserResponseDTO> assignNewRoleResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/user/assign-role"),
                HttpMethod.POST,
                assignRoleRequestEntity,
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

    private CourseResponseDTO createCourse(String adminJwtToken) {
        CourseDTO createCourseDTO = CourseDTO.builder()
                .name("Computer Science")
                .startDate(LocalDate.now())
                .status(CourseStatus.WAIT)
                .instructorEmail("steve-instructor@gmail.com")
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
        assertEquals(HttpStatus.OK, createdCourseResponse.getStatusCode());
        assertEquals(createCourseDTO.getName(), createdCourseResponse.getBody().getCourseName());
        assertNotNull(createdCourseResponse.getBody());
        return createdCourseResponse.getBody();
    }

    private GetHomeworkDTO findHomeworkByStudentAndCourse(Long lessonId, Long userId, String bearerToken) {
        GetHomeworkByLessonDTO dto = GetHomeworkByLessonDTO.builder()
                .lessonId(lessonId)
                .userId(userId)
                .build();
        adminHeaders.setBearerAuth(bearerToken);
        adminHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        ResponseEntity<GetHomeworkDTO> homeworkByStudentAndCourseResponse = restTemplate.exchange(
                buildUrl("/api/v1/homework"),
                HttpMethod.POST,
                new HttpEntity<>(dto, adminHeaders),
                GetHomeworkDTO.class
        );
        assertEquals(HttpStatus.OK, homeworkByStudentAndCourseResponse.getStatusCode());
        assertNotNull(homeworkByStudentAndCourseResponse.getBody());
        return homeworkByStudentAndCourseResponse.getBody();
    }

    private void addFeedbackForCourse(CourseResponseDTO createdCourse, UserResponseDTO foundStudentResponse) {
        CourseFeedbackDTO feedbackDTO = new CourseFeedbackDTO();
        feedbackDTO.setCourseId(createdCourse.getCourseId());
        feedbackDTO.setStudentId(foundStudentResponse.getId());
        feedbackDTO.setFeedbackText("Great, thanks!");

        HttpEntity<CourseFeedbackDTO> addFeedbackRequestEntity = new HttpEntity<>(feedbackDTO, adminHeaders);

        ResponseEntity<GetCourseFeedbackDTO> addFeedbackResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/feedback"),
                HttpMethod.POST,
                addFeedbackRequestEntity,
                GetCourseFeedbackDTO.class
        );

        assertEquals(feedbackDTO.getFeedbackText(), Objects.requireNonNull(addFeedbackResponseEntity.getBody()).getFeedbackText());
    }

    private GetHomeworkDTO setMarkForHomework(UserResponseDTO foundStudentResponse, String jwtToken,
                                              GetHomeworkDTO existedHomework, Long mark) {
        HomeworkDTO homeworkDTO = new HomeworkDTO();
        homeworkDTO.setUserId(foundStudentResponse.getId());
        homeworkDTO.setHomeworkId(existedHomework.getId());
        homeworkDTO.setMark(mark);

        adminHeaders.setBearerAuth(jwtToken);
        adminHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        adminHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<HomeworkDTO> setMarkRequestEntity = new HttpEntity<>(homeworkDTO, adminHeaders);

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
        adminHeaders.setBearerAuth(studentJwtToken);
        adminHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource("output.csv"));
        body.add("userId", foundStudentResponse.getId());
        body.add("lessonId", homeworkDTO.getLesson().getId());

        HttpEntity<MultiValueMap<String, Object>> uploadHomeworkRequestEntity = new HttpEntity<>(body, adminHeaders);

        ResponseEntity<FileResponseDTO> uploadHomeworkResponseEntity = restTemplate.exchange(
                buildUrl("/api/v1/files/upload"),
                HttpMethod.POST,
                uploadHomeworkRequestEntity,
                FileResponseDTO.class
        );
        assertEquals(HttpStatus.OK, uploadHomeworkResponseEntity.getStatusCode());
        return uploadHomeworkResponseEntity.getBody();
    }
    private String buildUrl(String path, Object... uriVariables) {
        return UriComponentsBuilder.fromUriString("http://localhost:" + port + path).buildAndExpand(uriVariables).toUriString();
    }
}
