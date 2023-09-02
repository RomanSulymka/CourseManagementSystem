package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.exception.UserAlreadyExistsException;
import edu.sombra.coursemanagementsystem.exception.UserNotFoundException;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findUserById(Long id) {
       return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public Long register(User user) {
        if (userRepository.existsUserByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException(user.getEmail());
        } else {
            user.setRole(RoleEnum.ADMIN);
            userRepository.save(user);
            return userRepository.findUserByEmail(user.getEmail()).getId();
        }
    }


}
