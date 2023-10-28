package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;

import java.util.List;

public interface UserRepository extends BaseRepository<User, Long> {
    boolean existsUserByEmail(String email);
    User findUserByEmail(String email);
    void updateRoleByEmail(String email, RoleEnum role);
    List<User> findUsersByEmails(List<String> emails);
}
