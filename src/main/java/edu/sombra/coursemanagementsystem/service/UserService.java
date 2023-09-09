package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.UserDTO;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;

import java.util.List;

public interface UserService {
    User findUserById(Long id);
    Long register(User user);
    String assignNewRole(UserDTO userDTO);
    List<User> findUsersByEmails(List<String> usersEmails);
    void validateInstructors(List<User> instructors, RoleEnum role);
}
