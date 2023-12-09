package edu.sombra.coursemanagementsystem.controller;

import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentApplyForCourseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentGetByNameDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentGetDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentResponseDTO;
import edu.sombra.coursemanagementsystem.dto.enrollment.EnrollmentUpdateDTO;
import edu.sombra.coursemanagementsystem.service.EnrollmentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/enrollment")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @PostMapping("/instructor")
    public ResponseEntity<EnrollmentResponseDTO> assignInstructor(@RequestBody EnrollmentDTO enrollmentDTO) {
        return ResponseEntity.ok(enrollmentService.assignInstructor(enrollmentDTO));
    }

    @PostMapping("/user/apply")
    public ResponseEntity<EnrollmentResponseDTO> applyForCourse(@RequestBody EnrollmentApplyForCourseDTO applyForCourseDTO,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(enrollmentService.applyForCourse(applyForCourseDTO, userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentGetDTO> getEnrollment(@PathVariable Long id)  {
        return ResponseEntity.ok(enrollmentService.findEnrolmentById(id));
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<List<EnrollmentGetByNameDTO>> getByName(@PathVariable String name)  {
        return ResponseEntity.ok(enrollmentService.findEnrolmentByCourseName(name));
    }

    @PutMapping
    public ResponseEntity<EnrollmentGetByNameDTO> update(@RequestBody EnrollmentUpdateDTO updateDTO) {
        return ResponseEntity.ok(enrollmentService.updateEnrollment(updateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeUserFromCourse(@PathVariable Long id) {
        enrollmentService.removeUserFromCourse(id);
        return ResponseEntity.ok("Instructor removed successfully");
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<CourseResponseDTO>> listAllCoursesByUser(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.findAllCoursesByUser(id));
    }
}
