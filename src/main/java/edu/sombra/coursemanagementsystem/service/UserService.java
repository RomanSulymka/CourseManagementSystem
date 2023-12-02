package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.user.CreateUserDTO;
import edu.sombra.coursemanagementsystem.dto.user.ResetPasswordDTO;
import edu.sombra.coursemanagementsystem.dto.user.UpdateUserDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserResponseDTO;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;

import java.util.List;

public interface UserService {
    UserResponseDTO findUserById(Long id);

    String assignNewRole(UserDTO userDTO);

    UserResponseDTO findUserByEmail(String email);

    void validateInstructor(User instructor, RoleEnum role);

    UserResponseDTO createUser(CreateUserDTO userDTO);

    UserResponseDTO updateUser(UpdateUserDTO userDTO);

    String resetPassword(ResetPasswordDTO resetPasswordDTO);

    String deleteUser(Long id);

    List<UserResponseDTO> findAllUsers();

    boolean existsUserByEmail(String email);

    boolean isUserInstructor(Long instructorId);
}
