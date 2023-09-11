package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsUserByEmail(String email);
    User findUserByEmail(String email);
}
