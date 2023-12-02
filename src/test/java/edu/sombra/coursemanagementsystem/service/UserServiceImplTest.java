package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.user.CreateUserDTO;
import edu.sombra.coursemanagementsystem.dto.user.ResetPasswordDTO;
import edu.sombra.coursemanagementsystem.dto.user.UpdateUserDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserResponseDTO;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.EntityDeletionException;
import edu.sombra.coursemanagementsystem.exception.UserCreationException;
import edu.sombra.coursemanagementsystem.exception.UserException;
import edu.sombra.coursemanagementsystem.exception.UserUpdateException;
import edu.sombra.coursemanagementsystem.mapper.UserMapper;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
    @Mock
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder, userMapper);
    }

    private static Stream<Arguments> provideTestDataForAssignNewRoleUpdateFailed() {
        UserDTO validUserDTO = UserDTO.builder()
                .email("user@example.com")
                .role(RoleEnum.ADMIN)
                .build();
        User mockUser = mock(User.class);

        return Stream.of(
                Arguments.of(validUserDTO, mockUser, "Failed assign new role for user"),
                Arguments.of(validUserDTO, mockUser, "Failed assign new role for user")
        );
    }

    private static Stream<Arguments> provideTestDataForCreateUserSuccessfully() {
        User validUser = User.builder()
                .id(1L)
                .firstName("firstName")
                .lastName("lastName")
                .role(RoleEnum.STUDENT)
                .password("validPassword")
                .email("user@example.com")
                .build();

        User anotherValidUser = User.builder()
                .id(2L)
                .firstName("anotherFirstName")
                .lastName("anotherLastName")
                .role(RoleEnum.INSTRUCTOR)
                .password("anotherValidPassword")
                .email("anotherUser@example.com")
                .build();

        return Stream.of(
                Arguments.of(validUser),
                Arguments.of(anotherValidUser)
        );
    }

    private static Stream<Arguments> provideTestDataForCreateUserDTO() {
        CreateUserDTO validUser = CreateUserDTO.builder()
                .firstName("firstName")
                .lastName("lastName")
                .role(RoleEnum.STUDENT)
                .password("validPassword")
                .email("user@example.com")
                .build();

        CreateUserDTO anotherValidUser = CreateUserDTO.builder()
                .firstName("anotherFirstName")
                .lastName("anotherLastName")
                .role(RoleEnum.INSTRUCTOR)
                .password("anotherValidPassword")
                .email("anotherUser@example.com")
                .build();

        return Stream.of(
                Arguments.of(validUser),
                Arguments.of(anotherValidUser)
        );
    }

    private static Stream<Arguments> provideTestDataForUpdateUserSuccessfully() {
        User existingUserStudentRole = User.builder()
                .id(1L)
                .firstName("firstName")
                .lastName("lastName")
                .role(RoleEnum.STUDENT)
                .password("validPassword")
                .email("user@example.com")
                .build();

        User existingUserInstructorRole = User.builder()
                .id(2L)
                .firstName("firstName")
                .lastName("lastName")
                .role(RoleEnum.INSTRUCTOR)
                .password("validPassword")
                .email("user@example.com")
                .build();

        UpdateUserDTO updateUser = UpdateUserDTO.builder()
                .id(1L)
                .firstName("firstName")
                .lastName("lastName")
                .role(RoleEnum.INSTRUCTOR)
                .password("validPassword")
                .email("user@example.com")
                .build();

        return Stream.of(
                Arguments.of(existingUserStudentRole, updateUser),
                Arguments.of(existingUserInstructorRole, updateUser)
        );
    }

    private static Stream<Arguments> provideTestUser() {
        String userEmail1 = "user@example.com";
        User mockUser1 = User.builder()
                .id(1L)
                .email("user@example.com")
                .build();

        String userEmail2 = "another@example.com";
        User mockUser2 = User.builder()
                .id(2L)
                .email("another@example.com")
                .build();

        return Stream.of(
                Arguments.of(userEmail1, mockUser1),
                Arguments.of(userEmail2, mockUser2)
        );
    }

    @Test
    void testFindUserByIdWhenUserExists() {
        Long userId = 1L;
        User expectedUser = User.builder()
                .id(1L)
                .build();

        UserResponseDTO expectedUserResponseDTO = UserResponseDTO.builder()
                .id(1L)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));
        when(userMapper.mapToResponseDTO(expectedUser)).thenReturn(expectedUserResponseDTO);

        UserResponseDTO foundUser = userService.findUserById(userId);

        assertNotNull(foundUser);
        assertEquals(expectedUserResponseDTO, foundUser);
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

    @ParameterizedTest
    @MethodSource("provideTestDataForAssignNewRoleUpdateFailed")
    void testAssignNewRoleUpdateFailed(UserDTO userDTO, User mockUser, String errorMessage) {
        when(userRepository.findUserByEmail(userDTO.getEmail())).thenReturn(mockUser);
        doThrow(new UserException(errorMessage)).when(userRepository)
                .updateRoleByEmail(userDTO.getEmail(), userDTO.getRole());

        UserException exception = assertThrows(UserException.class,
                () -> userService.assignNewRole(userDTO));

        assertEquals(errorMessage, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideTestUser")
    void testFindUserByEmailSuccessfully(String userEmail, User mockUser) {
        when(userRepository.findUserByEmail(userEmail)).thenReturn(mockUser);
        when(userMapper.mapToResponseDTO(mockUser)).thenReturn(mock(UserResponseDTO.class));
        UserResponseDTO resultUser = userService.findUserByEmail(userEmail);

        assertNotNull(resultUser);
        verify(userRepository, times(1)).findUserByEmail(userEmail);
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

    @ParameterizedTest
    @MethodSource("provideTestDataForCreateUserDTO")
    void testCreateUserSuccessfully(CreateUserDTO userDTO) {
        User user = User.builder()
                .id(1L)
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .role(userDTO.getRole())
                .password("encodedPassword")
                .email(userDTO.getEmail())
                .build();

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setFirstName(user.getFirstName());
        userResponseDTO.setLastName(user.getLastName());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setRole(user.getRole());

        when(userMapper.fromDTO(userDTO)).thenReturn(user);
        when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(user);
        when(userMapper.mapToResponseDTO(user)).thenReturn(userResponseDTO);
        UserResponseDTO createdUser = userService.createUser(userDTO);

        assertNotNull(createdUser);
        assertEquals(userResponseDTO, createdUser);
        verify(userRepository, times(1)).save(user);
        verify(userRepository, times(1)).findUserByEmail(user.getEmail());
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForCreateUserDTO")
    void testCreateUserWithEmptyPassword(CreateUserDTO userDTO) {
        userDTO.setPassword("");

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> userService.createUser(userDTO));

        assertEquals("Failed to create new User, the field is empty", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForCreateUserDTO")
    void testCreateUserWithEmptyUsername(CreateUserDTO userDTO) {
        userDTO.setLastName("");
        userDTO.setFirstName(" ");

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> userService.createUser(userDTO));

        assertEquals("Failed to create new User, the field is empty", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForCreateUserDTO")
    void testCreateUserWithEmptyEmail(CreateUserDTO userDTO) {
        userDTO.setEmail("");

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> userService.createUser(userDTO));

        assertEquals("Failed to create new User, the field is empty", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForCreateUserDTO")
    void testCreateUserWithNullRole(CreateUserDTO userDTO) {
        userDTO.setRole(null);

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> userService.createUser(userDTO));

        assertEquals("Failed to create new User, the field is empty", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForCreateUserDTO")
    void testCreateUserWithUserCreationException(CreateUserDTO userDTO) {
        User user = User.builder()
                .id(1L)
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .role(userDTO.getRole())
                .password("encodedPassword")
                .email(userDTO.getEmail())
                .build();

        when(userMapper.fromDTO(userDTO)).thenReturn(user);
        doThrow(UserCreationException.class)
                .when(userRepository)
                .save(user);

        UserCreationException exception = assertThrows(UserCreationException.class,
                () -> userService.createUser(userDTO));

        assertEquals("Failed to create user", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForUpdateUserSuccessfully")
    void testUpdateUserSuccessfully(User existingUser, UpdateUserDTO updateUser) {
        when(userRepository.findById(updateUser.getId())).thenReturn(Optional.ofNullable(existingUser));
        when(userRepository.update(existingUser)).thenReturn(existingUser);
        UserResponseDTO mockResponse = mock(UserResponseDTO.class);
        when(userMapper.mapToResponseDTO(existingUser)).thenReturn(mockResponse);

        UserResponseDTO updatedUser = userService.updateUser(updateUser);

        assertNotNull(updatedUser);
        assertEquals(existingUser.getEmail(), updateUser.getEmail());
        assertEquals(existingUser.getId(), updateUser.getId());
        verify(userRepository, times(1)).update(existingUser);
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForUpdateUserSuccessfully")
    void testUpdateUserWithNullRole(User existingUser, UpdateUserDTO updateUser) {
        updateUser.setRole(null);

        when(userRepository.findById(updateUser.getId())).thenReturn(Optional.ofNullable(existingUser));
        when(userRepository.update(existingUser)).thenReturn(existingUser);
        UserResponseDTO mockResponse = UserResponseDTO.builder()
                .id(existingUser.getId())
                .firstName(existingUser.getFirstName())
                .lastName(existingUser.getLastName())
                .role(existingUser.getRole())
                .email(existingUser.getEmail())
                .build();

        when(userMapper.mapToResponseDTO(existingUser)).thenReturn(mockResponse);

        UserResponseDTO updatedUser = userService.updateUser(updateUser);

        assertNotNull(updatedUser);
        assertEquals(existingUser.getRole(), updatedUser.getRole());
        verify(userRepository, times(1)).update(existingUser);
    }

    @Test
    void testUpdateUserWithUserUpdateException() {
        UpdateUserDTO updateUser = mock(UpdateUserDTO.class);

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

        when(userMapper.mapToResponseDTO(existingUser)).thenReturn(mock(UserResponseDTO.class));
        when(userRepository.findUserByEmail(resetPasswordDTO.getEmail())).thenReturn(existingUser);
        when(passwordEncoder.encode(resetPasswordDTO.getNewPassword())).thenReturn("encodedPassword");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));
        when(userMapper.mapToUpdateDTO(existingUser)).thenReturn(mock(UpdateUserDTO.class));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));
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
        doNothing().when(userRepository).delete(existingUser);

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

        List<UserResponseDTO> expectedUserResponseDTOs = List.of(mock(UserResponseDTO.class), mock(UserResponseDTO.class));

        when(userRepository.findAll()).thenReturn(expectedUsers);
        when(userMapper.mapToResponsesDTO(expectedUsers)).thenReturn(expectedUserResponseDTOs);

        List<UserResponseDTO> actualUsers = userService.findAllUsers();

        assertEquals(expectedUsers.size(), actualUsers.size());
        assertTrue(actualUsers.containsAll(expectedUserResponseDTOs));
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
