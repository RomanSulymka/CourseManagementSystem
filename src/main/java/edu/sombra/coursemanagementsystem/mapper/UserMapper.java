package edu.sombra.coursemanagementsystem.mapper;

import edu.sombra.coursemanagementsystem.dto.user.UserAssignedToCourseDTO;
import edu.sombra.coursemanagementsystem.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public List<UserAssignedToCourseDTO> mapUsersToDTO(List<User> users) {
        return users.stream()
                .map(this::mapUserToDTO)
                .collect(Collectors.toList());
    }

    public UserAssignedToCourseDTO mapUserToDTO(User user) {
        return UserAssignedToCourseDTO.builder()
                .id(user.getId())
                .lastName(user.getLastName())
                .firstName(user.getFirstName())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .build();
    }
}