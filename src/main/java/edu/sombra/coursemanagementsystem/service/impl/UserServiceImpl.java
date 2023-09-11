package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.ResetPasswordDTO;
import edu.sombra.coursemanagementsystem.dto.UserDTO;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.UserAlreadyExistsException;
import edu.sombra.coursemanagementsystem.exception.UserNotFoundException;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public String assignNewRole(UserDTO userDTO) {
         userRepository.updateRoleByEmail(userDTO.getEmail(), userDTO.getRole());
         return userDTO.getRole().name();
    }

    @Override
    public List<User> findUsersByEmails(List<String> usersEmails) {
        return userRepository.findUsersByEmails(usersEmails)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public User findUsersByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public void validateInstructors(List<User> instructors, RoleEnum role) {
        boolean allInstructors = instructors.stream()
                .allMatch(instructor -> instructor.getRole() == role);

        if (!allInstructors) {
            throw new AccessDeniedException("Users should have the role: " + role.name());
        }
    }

    @Override
    public User createUser(User user) {
        if (userRepository.existsUserByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException(user.getEmail());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Optional<User> updateUser(User user) {
        if (Objects.nonNull(findUserById(user.getId()))) {
            return userRepository.updateUser(user);
        }
        return Optional.empty();
    }

    @Override
    public String resetPassword(ResetPasswordDTO resetPasswordDTO) {
        if (Objects.nonNull(findUsersByEmail(resetPasswordDTO.getEmail()))) {
            User user = findUsersByEmail(resetPasswordDTO.getEmail());
            user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
            updateUser(user);
            log.info("Password has been changed successfully");
            return "Password changed!";
        }
        throw new UserNotFoundException("User not found: " + resetPasswordDTO.getEmail());
    }
}
