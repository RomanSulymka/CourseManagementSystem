package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    boolean existsUserByEmail(String email);
    User findUserByEmail(String email);
    void updateRoleByEmail(String email, RoleEnum role);
    Optional<List<User>> findUsersByEmails(List<String> emails);
    Optional<User> findById(Long id);
}
