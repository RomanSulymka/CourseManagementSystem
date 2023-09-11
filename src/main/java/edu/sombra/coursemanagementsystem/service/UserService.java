package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.ResetPasswordDTO;
import edu.sombra.coursemanagementsystem.dto.UserDTO;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User findUserById(Long id);
    String assignNewRole(UserDTO userDTO);
    List<User> findUsersByEmails(List<String> usersEmails);
    User findUsersByEmail(String email);
    void validateInstructors(List<User> instructors, RoleEnum role);
    User createUser(User user);
    Optional<User> updateUser(User user);
    String resetPassword(ResetPasswordDTO resetPasswordDTO);

}
