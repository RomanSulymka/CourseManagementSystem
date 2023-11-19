package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.user.ResetPasswordDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserDTO;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.EntityDeletionException;
import edu.sombra.coursemanagementsystem.exception.UserCreationException;
import edu.sombra.coursemanagementsystem.exception.UserException;
import edu.sombra.coursemanagementsystem.exception.UserUpdateException;
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
import org.springframework.validation.annotation.Validated;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: " + id);
                    return new EntityNotFoundException("User not found with id: " + id);
                });
    }

    @Override
    public String assignNewRole(UserDTO userDTO) {
        try {
            findUserByEmail(userDTO.getEmail());
            userRepository.updateRoleByEmail(userDTO.getEmail(), userDTO.getRole());
            return userDTO.getRole().name();
        } catch (UserException ex) {
            log.error("Error assigning new role for user with email: " + userDTO.getEmail());
            throw new UserException("Failed assign new role for user", ex);
        }
    }

    @Override
    public User findUserByEmail(String email) {
        try {
            return userRepository.findUserByEmail(email);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("User not found", e);
        }
    }

    @Override
    public void validateInstructor(User instructor, RoleEnum role) {
        if (instructor.getRole() != role) {
            log.error("User should has the role: " + role.name());
            throw new AccessDeniedException("User should has the role: " + role.name());
        }
    }

    @Override
    public User createUser(User user) {
        try {
            validateUser(user);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepository.save(user);
        } catch (UserCreationException ex) {
            log.error("Error creating user: {}", ex.getMessage(), ex);
            throw new UserCreationException("Failed to create user", ex);
        } catch (NullPointerException ex) {
            log.error("Failed to create new User, the field is empty");
            throw new NullPointerException("Failed to create new User, the field is empty");
        }
    }

    @Validated
    @Override
    public User updateUser(User user) {
        try {
            User existingUser = findUserById(user.getId());
            if (user.getRole() == null) {
                user.setRole(existingUser.getRole());
            }
            BeanUtils.copyProperties(user, existingUser, getNullPropertyNames(user));
            userRepository.update(existingUser);
            return existingUser;
        } catch (NullPointerException ex) {
            log.error("Error updating user with id: {}", user.getId(), ex);
            throw new UserUpdateException("Failed to update user", ex);
        }
    }

    @Override
    public String resetPassword(ResetPasswordDTO resetPasswordDTO) {
        if (Objects.nonNull(findUserByEmail(resetPasswordDTO.getEmail()))) {
            User user = findUserByEmail(resetPasswordDTO.getEmail());
            user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
            updateUser(user);
            log.info("Password has been changed successfully");
            return "Password changed!";
        }
        throw new EntityNotFoundException("User not found: " + resetPasswordDTO.getEmail());
    }

    @Override
    public String deleteUser(Long id) {
        try {
            User user = findUserById(id);
            userRepository.delete(user);
            log.info("User deleted successfully");
            return "User deleted successfully!";
        } catch (EntityNotFoundException ex) {
            log.error("Error deleting user with id: {}", id, ex);
            throw new EntityDeletionException("Failed to delete user", ex);
        }
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean existsUserByEmail(String email) {
        return userRepository.existsUserByEmail(email);
    }

    @Override
    public boolean isUserInstructor(Long instructorId) {
        User instructor = findUserById(instructorId);
        validateInstructor(instructor, RoleEnum.INSTRUCTOR);
        return true;
    }

    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        return Arrays.stream(pds)
                .map(PropertyDescriptor::getName)
                .filter(name -> src.getPropertyValue(name) == null)
                .toArray(String[]::new);
    }

    private void validateUser(User user) {
        if (user.getPassword().isEmpty()) {
            throw new NullPointerException("Password is empty!");
        } else if (user.getFirstName().isEmpty() || user.getLastName().isEmpty()) {
            throw new NullPointerException("Username is empty!");
        } else if (user.getEmail().isEmpty()) {
            throw new NullPointerException("Email is empty!");
        } else if (user.getRole() == null) {
            throw new NullPointerException("User Role is empty!");
        }
    }
}
