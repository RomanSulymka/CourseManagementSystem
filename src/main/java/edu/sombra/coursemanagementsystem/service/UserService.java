package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.user.ResetPasswordDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserDTO;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;

import java.util.List;

public interface UserService {
    User findUserById(Long id);

    String assignNewRole(UserDTO userDTO);

    List<User> findUsersByEmails(List<String> usersEmails);

    User findUserByEmail(String email);

    void validateInstructor(User instructor, RoleEnum role);

    User createUser(User user);

    User updateUser(User user);

    String resetPassword(ResetPasswordDTO resetPasswordDTO);

    boolean deleteUser(Long id);

    List<User> findAllUsers();

    boolean existsUserByEmail(String email);

    boolean isUserInstructor(Long instructorId);

    boolean isInstructorAssignedToCourse(Long instructorId, Long courseId);

    boolean isStudentAssignedToCourse(Long studentId, Long courseId);
}
