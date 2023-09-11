package edu.sombra.coursemanagementsystem.controller;

import edu.sombra.coursemanagementsystem.dto.EnrollmentDTO;
import edu.sombra.coursemanagementsystem.service.EnrollmentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/enrollment")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @PostMapping("/instructor")
    public ResponseEntity<String> setInstructor(@RequestBody EnrollmentDTO enrollmentDTO) {
        enrollmentService.assignInstructor(enrollmentDTO);
        return ResponseEntity.ok("Instructor assigned successfully.");
    }
}
