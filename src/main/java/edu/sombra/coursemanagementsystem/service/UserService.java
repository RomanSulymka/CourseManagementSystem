package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.user.CreateUserDTO;
import edu.sombra.coursemanagementsystem.dto.user.ResetPasswordDTO;
import edu.sombra.coursemanagementsystem.dto.user.UpdateUserDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserResponseDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO findUserById(Long id);

    UserResponseDTO assignNewRole(UserDTO userDTO);

    UserResponseDTO findUserByEmail(String email);

    UserResponseDTO createUser(CreateUserDTO userDTO);

    UserResponseDTO updateUser(UpdateUserDTO userDTO, String userEmail);

    String resetPassword(ResetPasswordDTO resetPasswordDTO, String userEmail);

    void deleteUser(Long id);

    List<UserResponseDTO> findAllUsers();

    boolean existsUserByEmail(String email);

    boolean isUserInstructor(Long instructorId);
}
