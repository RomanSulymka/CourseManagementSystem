package edu.sombra.coursemanagementsystem.service.impl;

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
import edu.sombra.coursemanagementsystem.mapper.UserMapper;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.UserService;
import edu.sombra.coursemanagementsystem.util.BaseUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@AllArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    public static final String USER_NOT_FOUND_WITH_ID = "User not found with id: ";
    public static final String ERROR_ASSIGNING_NEW_ROLE_FOR_USER_WITH_EMAIL = "Error assigning new role for user with email: ";
    public static final String FAILED_ASSIGN_NEW_ROLE_FOR_USER = "Failed assign new role for user";
    public static final String USER_NOT_FOUND = "User not found: ";
    public static final String USER_SHOULD_HAVE_THE_ROLE = "User should have the role: ";
    public static final String FAILED_TO_CREATE_USER = "Failed to create user ";
    public static final String FAILED_TO_UPDATE_USER = "Failed to update user ";
    public static final String PASSWORD_HAS_BEEN_CHANGED_SUCCESSFULLY = "Password has been changed successfully";
    public static final String PASSWORD_CHANGED = "Password changed!";
    public static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully";
    public static final String FAILED_TO_DELETE_USER = "Failed to delete user ";
    public static final String PASSWORD_IS_EMPTY = "Password is empty!";
    public static final String USERNAME_IS_EMPTY = "Username is empty!";
    public static final String EMAIL_IS_EMPTY = "Email is empty!";
    public static final String USER_ROLE_IS_EMPTY = "User Role is empty!";
    public static final String ERROR_USER_NOT_FOUND = "User not found: ";
    public static final String ROLE_NOT_FOUND = "Role not found: ";
    public static final String FAILED_TO_RESET_PASSWORD = "Failed to reset password: ";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    @Override
    public UserResponseDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_WITH_ID + id);
                    return new EntityNotFoundException(USER_NOT_FOUND_WITH_ID + id);
                });
        return mapper.mapToResponseDTO(user);
    }

    @Override
    public UserResponseDTO assignNewRole(UserDTO userDTO) {
        try {
            findUserByEmail(userDTO.getEmail());

            if (!Arrays.asList(RoleEnum.values()).contains(userDTO.getRole())) {
                throw new IllegalArgumentException(ROLE_NOT_FOUND + userDTO.getRole());
            }

            userRepository.updateRoleByEmail(userDTO.getEmail(), userDTO.getRole());

            User user = userRepository.findUserByEmail(userDTO.getEmail());

            return mapper.mapToResponseDTO(user);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new UserException(FAILED_ASSIGN_NEW_ROLE_FOR_USER, ex);
        }
    }

    @Override
    public UserResponseDTO findUserByEmail(String email) {
        try {
            User user = userRepository.findUserByEmail(email);
            return mapper.mapToResponseDTO(user);
        } catch (NoResultException e) {
            throw new UserCreationException(e);
        }
    }

    @Override
    public void validateInstructor(User instructor, RoleEnum role) {
        if (instructor.getRole() != role) {
            log.error(USER_SHOULD_HAVE_THE_ROLE + role.name());
            throw new AccessDeniedException(USER_SHOULD_HAVE_THE_ROLE + role.name());
        }
    }

    @Override
    public UserResponseDTO createUser(CreateUserDTO userDTO) {
        try {
            validateUser(userDTO);
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            User user = mapper.fromDTO(userDTO);
            userRepository.save(user);
            return findUserByEmail(user.getEmail());
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new UserCreationException(FAILED_TO_CREATE_USER, ex);
        }
    }

    @Validated
    @Override
    @Transactional
    public UserResponseDTO updateUser(UpdateUserDTO userDTO, String userEmail) {
        User loggedUser = userRepository.findUserByEmail(userEmail);

        if (loggedUser.getRole().equals(RoleEnum.ADMIN)) {
            return updateUserAsAdmin(userDTO);
        } else {
            return updateUserAsNonAdmin(userDTO, loggedUser);
        }
    }

    private UserResponseDTO updateUserAsAdmin(UpdateUserDTO userDTO) {
        User existingUser = userRepository.findById(userDTO.getId())
                .orElseThrow(EntityNotFoundException::new);

        if (userDTO.getRole() == null) {
            userDTO.setRole(existingUser.getRole());
        }

        BeanUtils.copyProperties(userDTO, existingUser, BaseUtil.getNullPropertyNames(userDTO));
        userRepository.update(existingUser);
        return mapper.mapToResponseDTO(existingUser);
    }

    private UserResponseDTO updateUserAsNonAdmin(UpdateUserDTO userDTO, User loggedUser) {
        if (loggedUser.getId().equals(userDTO.getId())) {
            userDTO.setRole(loggedUser.getRole());
            BeanUtils.copyProperties(userDTO, loggedUser, BaseUtil.getNullPropertyNames(userDTO));
            userRepository.update(loggedUser);
            return mapper.mapToResponseDTO(loggedUser);
        } else {
            throw new AccessDeniedException(USER_SHOULD_HAVE_THE_ROLE + RoleEnum.ADMIN);
        }
    }

    @Override
    public String resetPassword(ResetPasswordDTO resetPasswordDTO, String userEmail) {
        try {
            if (Objects.nonNull(findUserByEmail(resetPasswordDTO.getEmail()))) {
                User user = userRepository.findUserByEmail(resetPasswordDTO.getEmail());
                user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
                updateUser(mapper.mapToUpdateDTO(user), userEmail);
                return PASSWORD_CHANGED;
            }
            throw new EntityNotFoundException(USER_NOT_FOUND + resetPasswordDTO.getEmail());
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new UserException(FAILED_TO_RESET_PASSWORD, ex);
        }
    }

    @Override
    public String deleteUser(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error(USER_NOT_FOUND_WITH_ID + id);
                        return new EntityNotFoundException(USER_NOT_FOUND_WITH_ID + id);
                    });
            userRepository.delete(user);
            log.info(USER_DELETED_SUCCESSFULLY);
            return USER_DELETED_SUCCESSFULLY;
        } catch (EntityNotFoundException ex) {
            log.error(ex.getMessage());
            throw new EntityDeletionException(FAILED_TO_DELETE_USER, ex);
        }
    }

    @Override
    public List<UserResponseDTO> findAllUsers() {
        List<User> users = userRepository.findAll();
        return mapper.mapToResponsesDTO(users);
    }

    @Override
    public boolean existsUserByEmail(String email) {
        return userRepository.existsUserByEmail(email);
    }

    @Override
    public boolean isUserInstructor(Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND + instructorId);
                    return new EntityNotFoundException(USER_NOT_FOUND_WITH_ID + instructorId);
                });
        validateInstructor(instructor, RoleEnum.INSTRUCTOR);
        return true;
    }

    private void validateUser(CreateUserDTO userDTO) {
        if (userDTO.getPassword().isEmpty() || userDTO.getPassword().isBlank()) {
            throw new NullPointerException(PASSWORD_IS_EMPTY);
        } else if (userDTO.getFirstName().isEmpty() || userDTO.getLastName().isEmpty()
                || userDTO.getFirstName().isBlank() || userDTO.getLastName().isBlank()) {
            throw new NullPointerException(USERNAME_IS_EMPTY);
        } else if (userDTO.getEmail().isEmpty() || userDTO.getEmail().isBlank()) {
            throw new NullPointerException(EMAIL_IS_EMPTY);
        } else if (userDTO.getRole() == null) {
            throw new NullPointerException(USER_ROLE_IS_EMPTY);
        }
    }
}
