package edu.sombra.coursemanagementsystem.service.impl;

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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public Long register(User user) {
        if (userRepository.existsUserByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException(user.getEmail());
        } else {
            user.setRole(RoleEnum.ADMIN);
            userRepository.save(user);
            return userRepository.findUserByEmail(user.getEmail()).getId();
        }
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
    public void validateInstructors(List<User> instructors, RoleEnum role) {
        boolean allInstructors = instructors.stream()
                .allMatch(instructor -> instructor.getRole() == role);

        if (!allInstructors) {
            throw new AccessDeniedException("Users should have the role: " + role.name());
        }
    }
}
