package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.entity.User;

public interface UserService {
    User findUserById(Long id);
    Long register(User user);
}
