package edu.sombra.coursemanagementsystem.controller.auth;

import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.dto.auth.RegisterDTO;
import edu.sombra.coursemanagementsystem.service.UserService;
import edu.sombra.coursemanagementsystem.service.auth.AuthenticateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping(("/api/v1/auth"))
public class AuthenticationController {
    private final AuthenticateService authenticateService;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationDTO authenticationDTO) {
        return ResponseEntity.ok(authenticateService.authenticate(authenticationDTO));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> registerUser(@RequestBody RegisterDTO registerDTO){
        return ResponseEntity.ok(authenticateService.register(registerDTO));
    }

    @PostMapping("/refresh-token")
    public void refreshJWTToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticateService.refreshToken(request, response);
    }
}
