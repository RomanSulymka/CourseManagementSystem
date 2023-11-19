package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.user.ResetPasswordDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserDTO;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.EntityDeletionException;
import edu.sombra.coursemanagementsystem.exception.UserCreationException;
import edu.sombra.coursemanagementsystem.exception.UserException;
import edu.sombra.coursemanagementsystem.exception.UserUpdateException;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void testFindUserByIdWhenUserExists() {
        Long userId = 1L;
        User expectedUser = User.builder()
                .id(1L)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        User foundUser = userService.findUserById(userId);

        assertNotNull(foundUser);
        assertEquals(expectedUser, foundUser);
    }

    @Test
    void testFindUserByIdWhenUserDoesNotExist() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.findUserById(userId));

        assertEquals("User not found with id: " + userId, exception.getMessage());
    }

    @Test
    void testAssignNewRoleSuccessfully() {
        UserDTO userDTO = UserDTO.builder()
                .email("user@example.com")
                .role(RoleEnum.ADMIN)
                .build();

        User mockUser = mock(User.class);

        when(userRepository.findUserByEmail(userDTO.getEmail())).thenReturn(mockUser);
        doNothing().when(userRepository).updateRoleByEmail(userDTO.getEmail(), userDTO.getRole());

        String assignedRole = userService.assignNewRole(userDTO);

        assertEquals(userDTO.getRole().name(), assignedRole);
    }

    @Test
    void testAssignNewRoleUserNotFound() {
        UserDTO userDTO = UserDTO.builder()
                .email("user@example.com")
                .role(RoleEnum.ADMIN)
                .build();

        when(userRepository.findUserByEmail(userDTO.getEmail())).thenThrow(EntityNotFoundException.class);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.assignNewRole(userDTO));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testAssignNewRoleUpdateFailed() {
        UserDTO userDTO = UserDTO.builder()
                .email("user@example.com")
                .role(RoleEnum.ADMIN)
                .build();

        User mockUser = mock(User.class);

        when(userRepository.findUserByEmail(userDTO.getEmail())).thenReturn(mockUser);
        doThrow(new UserException("Failed assign new role for user")).when(userRepository)
                .updateRoleByEmail(userDTO.getEmail(), userDTO.getRole());

        UserException exception = assertThrows(UserException.class,
                () -> userService.assignNewRole(userDTO));

        assertEquals("Failed assign new role for user", exception.getMessage());
    }

    @Test
    void testFindUserByEmailSuccessfully() {
        String userEmail = "user@example.com";
        User mockUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .build();

        when(userRepository.findUserByEmail(userEmail)).thenReturn(mockUser);

        User resultUser = userService.findUserByEmail(userEmail);

        assertNotNull(resultUser);
        assertEquals(mockUser, resultUser);
    }

    @Test
    void testFindUserByEmailUserNotFound() {
        String userEmail = "nonexistent@example.com";

        when(userRepository.findUserByEmail(userEmail)).thenThrow(new EntityNotFoundException("User not found"));

        assertThrows(EntityNotFoundException.class, () -> userService.findUserByEmail(userEmail));
    }

    @Test
    void testValidateInstructorWithCorrectRole() {
        User mockUser = User.builder()
                .id(1L)
                .role(RoleEnum.INSTRUCTOR)
                .email("user@example.com")
                .build();
        RoleEnum expectedRole = RoleEnum.INSTRUCTOR;

        assertDoesNotThrow(() -> userService.validateInstructor(mockUser, expectedRole));
    }

    @Test
    void testValidateInstructorWithIncorrectRole() {
        User mockUser = User.builder()
                .id(1L)
                .role(RoleEnum.STUDENT)
                .email("user@example.com")
                .build();
        RoleEnum expectedRole = RoleEnum.INSTRUCTOR;

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userService.validateInstructor(mockUser, expectedRole));

        assertEquals("User should has the role: " + expectedRole.name(), exception.getMessage());
    }

    @Test
    void testCreateUserSuccessfully() {
        User user = User.builder()
                .id(1L)
                .firstName("firstName")
                .lastName("lastName")
                .role(RoleEnum.STUDENT)
                .password("validPassword")
                .email("user@example.com")
                .build();

        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);

        User createdUser = userService.createUser(user);

        assertNotNull(createdUser);
        assertEquals("encodedPassword", createdUser.getPassword());
    }

    @Test
    void testCreateUserWithEmptyPassword() {
        User user = User.builder()
                .id(1L)
                .firstName("firstName")
                .lastName("lastName")
                .role(RoleEnum.STUDENT)
                .password("")
                .email("user@example.com")
                .build();

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> userService.createUser(user));

        assertEquals("Failed to create new User, the field is empty", exception.getMessage());
    }

    @Test
    void testCreateUserWithEmptyUsername() {
        User user = User.builder()
                .id(1L)
                .firstName("")
                .lastName("")
                .password("1111")
                .role(RoleEnum.STUDENT)
                .password(" ")
                .email("user@example.com")
                .build();

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> userService.createUser(user));

        assertEquals("Failed to create new User, the field is empty", exception.getMessage());
    }

    @Test
    void testCreateUserWithEmptyEmail() {
        User user = User.builder()
                .id(1L)
                .firstName("firstName")
                .lastName("lastName")
                .role(RoleEnum.STUDENT)
                .password("validPassword")
                .email("")
                .build();
        user.setEmail("");

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> userService.createUser(user));

        assertEquals("Failed to create new User, the field is empty", exception.getMessage());
    }

    @Test
    void testCreateUserWithNullRole() {
        User user = User.builder()
                .id(1L)
                .firstName("firstName")
                .lastName("lastName")
                .role(null)
                .password("validPassword")
                .email("user@example.com")
                .build();

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> userService.createUser(user));

        assertEquals("Failed to create new User, the field is empty", exception.getMessage());
    }

    @Test
    void testCreateUserWithUserCreationException() {
        User user = User.builder()
                .id(1L)
                .firstName("firstName")
                .lastName("lastName")
                .role(RoleEnum.STUDENT)
                .password("validPassword")
                .email("user@example.com")
                .build();
        doThrow(UserCreationException.class).when(userRepository).save(user);

        UserCreationException exception = assertThrows(UserCreationException.class,
                () -> userService.createUser(user));

        assertEquals("Failed to create user", exception.getMessage());
    }

    @Test
    void testUpdateUserSuccessfully() {
        User existingUser = User.builder()
                .id(1L)
                .firstName("firstName")
                .lastName("lastName")
                .role(RoleEnum.STUDENT)
                .password("validPassword")
                .email("user@example.com")
                .build();

        User updateUser = User.builder()
                .id(1L)
                .firstName("firstName")
                .lastName("lastName")
                .role(RoleEnum.INSTRUCTOR)
                .password("validPassword")
                .email("user@example.com")
                .build();

        when(userRepository.findById(updateUser.getId())).thenReturn(Optional.ofNullable(existingUser));
        when(userRepository.update(existingUser)).thenReturn(existingUser);

        User updatedUser = userService.updateUser(updateUser);

        assertNotNull(updatedUser);
        assertEquals(existingUser.getId(), updatedUser.getId());
    }

    @Test
    void testUpdateUserWithNullRole() {
        User existingUser = User.builder()
                .id(1L)
                .firstName("firstName")
                .lastName("lastName")
                .role(RoleEnum.STUDENT)
                .password("validPassword")
                .email("user@example.com")
                .build();

        User updateUser = User.builder()
                .id(1L)
                .firstName("firstName")
                .lastName("lastName")
                .role(null)
                .password("validPassword")
                .email("user@example.com")
                .build();

        when(userRepository.findById(updateUser.getId())).thenReturn(Optional.ofNullable(existingUser));
        when(userRepository.update(existingUser)).thenReturn(existingUser);

        User updatedUser = userService.updateUser(updateUser);

        assertNotNull(updatedUser);
        assertEquals(existingUser.getId(), updatedUser.getId());
        assertEquals(existingUser.getRole(), updatedUser.getRole());
    }

    @Test
    void testUpdateUserWithUserUpdateException() {
        User updateUser = new User();

        when(userRepository.findById(updateUser.getId())).thenReturn(null);

        UserUpdateException exception = assertThrows(UserUpdateException.class,
                () -> userService.updateUser(updateUser));

        assertEquals("Failed to update user", exception.getMessage());
    }

    @Test
    void testResetPasswordSuccess() {
        ResetPasswordDTO resetPasswordDTO = ResetPasswordDTO.builder()
                .email("user@example.com")
                .newPassword("newPassword123")
                .build();

        User existingUser = User.builder()
                .id(1L)
                .firstName("firstName")
                .lastName("lastName")
                .role(RoleEnum.STUDENT)
                .password("validPassword")
                .email("user@example.com")
                .build();
        when(userRepository.findUserByEmail(resetPasswordDTO.getEmail())).thenReturn(existingUser);
        when(passwordEncoder.encode(resetPasswordDTO.getNewPassword())).thenReturn("encodedPassword");
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.update(existingUser)).thenReturn(existingUser);

        String result = userService.resetPassword(resetPasswordDTO);

        assertEquals("Password changed!", result);
        verify(userRepository, times(1)).update(existingUser);
    }

    @Test
    void testResetPasswordUserNotFound() {
        ResetPasswordDTO resetPasswordDTO = ResetPasswordDTO.builder()
                .email("nonexistent@example.com")
                .newPassword("newPassword123")
                .build();

        when(userRepository.findUserByEmail(resetPasswordDTO.getEmail())).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.resetPassword(resetPasswordDTO));

        assertEquals("User not found: " + resetPasswordDTO.getEmail(), exception.getMessage());
        verify(userRepository, never()).update(any());
    }

    @Test
    void testDeleteUserSuccess() {
        Long userId = 1L;
        User existingUser = User.builder()
                .id(1L)
                .firstName("firstName")
                .lastName("lastName")
                .role(RoleEnum.STUDENT)
                .password("validPassword")
                .email("user@example.com")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        String result = userService.deleteUser(userId);

        assertEquals("User deleted successfully!", result);
        verify(userRepository, times(1)).delete(existingUser);
    }

    @Test
    void testDeleteUserNotFound() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityDeletionException exception = assertThrows(EntityDeletionException.class,
                () -> userService.deleteUser(userId));

        assertEquals("Failed to delete user", exception.getMessage());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void testDeleteUserNullId() {
        EntityDeletionException exception = assertThrows(EntityDeletionException.class,
                () -> userService.deleteUser(null));

        assertEquals("Failed to delete user", exception.getMessage());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void testFindAllUsers() {
        List<User> expectedUsers = List.of(
                mock(User.class),
                mock(User.class)
        );
        when(userRepository.findAll()).thenReturn(expectedUsers);

        List<User> actualUsers = userService.findAllUsers();

        assertEquals(expectedUsers.size(), actualUsers.size());
        assertTrue(actualUsers.containsAll(expectedUsers));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testExistsUserByEmailTrue() {
        String userEmail = "user@example.com";
        when(userRepository.existsUserByEmail(userEmail)).thenReturn(true);

        boolean result = userService.existsUserByEmail(userEmail);

        assertTrue(result);
        verify(userRepository, times(1)).existsUserByEmail(userEmail);
    }

    @Test
    void testExistsUserByEmailFalse() {
        String userEmail = "nonexistent@example.com";
        when(userRepository.existsUserByEmail(userEmail)).thenReturn(false);

        boolean result = userService.existsUserByEmail(userEmail);

        assertFalse(result);
        verify(userRepository, times(1)).existsUserByEmail(userEmail);
    }

    @Test
    void testExistsUserByEmailNull() {
        when(userRepository.existsUserByEmail(null)).thenReturn(false);

        boolean result = userService.existsUserByEmail(null);

        assertFalse(result);
        verify(userRepository, times(1)).existsUserByEmail(null);
    }

    @Test
    void testIsUserInstructorSuccess() {
        Long instructorId = 1L;
        User instructor = User.builder()
                .id(1L)
                .email("user@example.com")
                .role(RoleEnum.INSTRUCTOR)
                .build();
        when(userRepository.findById(instructorId)).thenReturn(Optional.of(instructor));

        boolean result = userService.isUserInstructor(instructorId);

        assertTrue(result);
        verify(userRepository, times(1)).findById(instructorId);
    }

    @Test
    void testIsUserInstructorNotFound() {
        Long instructorId = 1L;
        when(userRepository.findById(instructorId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.isUserInstructor(instructorId));

        assertEquals("User not found with id: " + instructorId, exception.getMessage());
        verify(userRepository, times(1)).findById(instructorId);
    }

    @Test
    void testIsUserInstructorWrongRole() {
        Long instructorId = 1L;
        User instructor = User.builder()
                .id(1L)
                .email("user@example.com")
                .role(RoleEnum.STUDENT)
                .build();
        when(userRepository.findById(instructorId)).thenReturn(Optional.of(instructor));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userService.isUserInstructor(instructorId));

        assertEquals("User should has the role: INSTRUCTOR", exception.getMessage());
        verify(userRepository, times(1)).findById(instructorId);
    }
}
