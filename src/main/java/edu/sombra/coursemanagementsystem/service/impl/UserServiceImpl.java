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
import edu.sombra.coursemanagementsystem.exception.UserUpdateException;
import edu.sombra.coursemanagementsystem.mapper.UserMapper;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.beans.PropertyDescriptor;
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
    public static final String USER_SHOULD_HAS_THE_ROLE = "User should has the role: ";
    public static final String FAILED_TO_CREATE_USER = "Failed to create user";
    public static final String FAILED_TO_CREATE_NEW_USER_THE_FIELD_IS_EMPTY = "Failed to create new User, the field is empty";
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
    public String assignNewRole(UserDTO userDTO) {
        try {
            findUserByEmail(userDTO.getEmail());
            userRepository.updateRoleByEmail(userDTO.getEmail(), userDTO.getRole());
            return userDTO.getRole().name();
        } catch (UserException ex) {
            log.error(ERROR_ASSIGNING_NEW_ROLE_FOR_USER_WITH_EMAIL + userDTO.getEmail());
            throw new UserException(FAILED_ASSIGN_NEW_ROLE_FOR_USER, ex);
        }
    }

    @Override
    public UserResponseDTO findUserByEmail(String email) {
        try {
            User user = userRepository.findUserByEmail(email);
            return mapper.mapToResponseDTO(user);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException(ERROR_USER_NOT_FOUND, e);
        }
    }

    @Override
    public void validateInstructor(User instructor, RoleEnum role) {
        if (instructor.getRole() != role) {
            log.error(USER_SHOULD_HAS_THE_ROLE + role.name());
            throw new AccessDeniedException(USER_SHOULD_HAS_THE_ROLE + role.name());
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
        } catch (UserCreationException ex) {
            log.error(FAILED_TO_CREATE_USER, ex.getMessage(), ex);
            throw new UserCreationException(FAILED_TO_CREATE_USER, ex);
        } catch (NullPointerException ex) {
            log.error(FAILED_TO_CREATE_NEW_USER_THE_FIELD_IS_EMPTY);
            throw new NullPointerException(FAILED_TO_CREATE_NEW_USER_THE_FIELD_IS_EMPTY);
        }
    }

    @Validated
    @Override
    public UserResponseDTO updateUser(UpdateUserDTO userDTO) {
        try {
            User existingUser = userRepository.findById(userDTO.getId())
                    .orElseThrow(() -> {
                        log.error(USER_NOT_FOUND_WITH_ID + userDTO.getId());
                        return new EntityNotFoundException(USER_NOT_FOUND_WITH_ID + userDTO.getId());
                    });
            if (userDTO.getRole() == null) {
                userDTO.setRole(existingUser.getRole());
            }
            BeanUtils.copyProperties(userDTO, existingUser, getNullPropertyNames(userDTO));
            userRepository.update(existingUser);
            return mapper.mapToResponseDTO(existingUser);
        } catch (NullPointerException ex) {
            log.error(FAILED_TO_UPDATE_USER + userDTO.getId() + ex);
            throw new UserUpdateException(FAILED_TO_UPDATE_USER, ex);
        }
    }

    @Override
    public String resetPassword(ResetPasswordDTO resetPasswordDTO) {
        if (Objects.nonNull(findUserByEmail(resetPasswordDTO.getEmail()))) {
            User user = userRepository.findUserByEmail(resetPasswordDTO.getEmail());
            user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
            updateUser(mapper.mapToUpdateDTO(user));
            log.info(PASSWORD_HAS_BEEN_CHANGED_SUCCESSFULLY);
            return PASSWORD_CHANGED;
        }
        throw new EntityNotFoundException(USER_NOT_FOUND + resetPasswordDTO.getEmail());
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
            log.error(FAILED_TO_DELETE_USER + id + ex);
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

    private static String[] getNullPropertyNames(Object entity) {
        final BeanWrapper src = new BeanWrapperImpl(entity);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        return Arrays.stream(pds)
                .map(PropertyDescriptor::getName)
                .filter(name -> src.getPropertyValue(name) == null)
                .toArray(String[]::new);
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
