package edu.sombra.coursemanagementsystem.service.auth;

import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.dto.auth.RegisterDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface AuthenticateService {
    AuthenticationResponse register(RegisterDTO registerDTO);

    AuthenticationResponse authenticate(AuthenticationDTO authenticationDTO);

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
