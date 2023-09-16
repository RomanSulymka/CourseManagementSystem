package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long> {
    boolean existsUserByEmail(String email);
    Optional<User> findUserByEmail(String email);
    void updateRoleByEmail(String email, RoleEnum role);
    Optional<List<User>> findUsersByEmails(List<String> emails);
}
