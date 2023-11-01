package edu.sombra.coursemanagementsystem.api;

import edu.sombra.coursemanagementsystem.controller.UserController;
import edu.sombra.coursemanagementsystem.dto.user.ResetPasswordDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserDTO;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserControllerTest {
    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    static Stream<User> userProvider() {
        return Stream.of(
                User.builder()
                        .email(null)
                        .firstName("test")
                        .lastName("user")
                        .role(RoleEnum.STUDENT)
                        .password("1234")
                        .build()
        );
    }

    @Test
    void testCreateUser() {
        User user = new User();
        Mockito.when(userService.createUser(user)).thenReturn(user);

        ResponseEntity<User> responseEntity = userController.create(user);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(user, responseEntity.getBody());
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        Mockito.when(userService.updateUser(user)).thenReturn(user);

        ResponseEntity<User> responseEntity = userController.update(user);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(user, responseEntity.getBody());
    }

    @Test
    void testAssignNewRole() {
        UserDTO userDTO = new UserDTO();
        String message = "Role assigned successfully";
        Mockito.when(userService.assignNewRole(userDTO)).thenReturn(message);

        ResponseEntity<String> responseEntity = userController.assignNewRole(userDTO);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(message, responseEntity.getBody());
    }

    @Test
    void testResetPassword() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        String message = "Password reset successful";
        Mockito.when(userService.resetPassword(resetPasswordDTO)).thenReturn(message);

        ResponseEntity<String> responseEntity = userController.resetPassword(resetPasswordDTO);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(message, responseEntity.getBody());
    }

    @Test
    void testFindUserById() {
        Long userId = 1L;
        User user = new User();
        Mockito.when(userService.findUserById(userId)).thenReturn(user);

        ResponseEntity<User> responseEntity = userController.findUserById(userId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(user, responseEntity.getBody());
    }

    @Test
    void testFindUserByEmail() {
        String email = "test@example.com";
        User user = new User();
        Mockito.when(userService.findUserByEmail(email)).thenReturn(user);

        ResponseEntity<User> responseEntity = userController.findUserByEmail(email);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(user, responseEntity.getBody());
    }

    @Test
    void testFindAllUsers() {
        List<User> userList = new ArrayList<>();
        Mockito.when(userService.findAllUsers()).thenReturn(userList);

        ResponseEntity<List<User>> responseEntity = userController.findAll();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(userList, responseEntity.getBody());
    }

    @Test
    void testDeleteUser() {
        Long userId = 1L;
        String message = "User deleted successfully";
        Mockito.when(userService.deleteUser(userId)).thenReturn(message);

        ResponseEntity<String> responseEntity = userController.delete(userId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(message, responseEntity.getBody());
    }
}