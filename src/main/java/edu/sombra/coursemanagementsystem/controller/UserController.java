package edu.sombra.coursemanagementsystem.controller;

import edu.sombra.coursemanagementsystem.dto.ResetPasswordDTO;
import edu.sombra.coursemanagementsystem.dto.UserDTO;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PutMapping("/update")
    public ResponseEntity<Optional<User>> updateUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(user));
    }

    @PostMapping("/assign")
    public ResponseEntity<String> assignNewRole(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.assignNewRole(userDTO));
    }

    @PutMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        return ResponseEntity.ok(userService.resetPassword(resetPasswordDTO));
    }
}
