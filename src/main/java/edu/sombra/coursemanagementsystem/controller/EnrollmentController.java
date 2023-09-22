package edu.sombra.coursemanagementsystem.controller;

import edu.sombra.coursemanagementsystem.dto.EnrollmentApplyForCourseDTO;
import edu.sombra.coursemanagementsystem.dto.EnrollmentDTO;
import edu.sombra.coursemanagementsystem.dto.EnrollmentGetByNameDTO;
import edu.sombra.coursemanagementsystem.dto.EnrollmentGetDTO;
import edu.sombra.coursemanagementsystem.dto.EnrollmentRequest;
import edu.sombra.coursemanagementsystem.dto.EnrollmentUpdateDTO;
import edu.sombra.coursemanagementsystem.service.EnrollmentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> setInstructor(@RequestBody EnrollmentDTO enrollmentDTO) {
        enrollmentService.  assignInstructor(enrollmentDTO);
        return ResponseEntity.ok("Instructor assigned successfully.");
    }

    @PostMapping("/user/apply")
    public ResponseEntity<String> applyForCourse(@RequestBody EnrollmentApplyForCourseDTO applyForCourseDTO) {
        enrollmentService.applyForCourse(applyForCourseDTO);
        return ResponseEntity.ok("User assigned successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentGetDTO> getEnrollment(@PathVariable Long id)  {
        return ResponseEntity.ok(enrollmentService.findEnrolmentById(id));
    }

    @PostMapping("/by-name")
    public ResponseEntity<List<EnrollmentGetByNameDTO>> getByName(@RequestBody EnrollmentRequest request)  {
        return ResponseEntity.ok(enrollmentService.findEnrolmentByCourseName(request.getName()));
    }

    @PutMapping
    public ResponseEntity<EnrollmentGetByNameDTO> update(@RequestBody EnrollmentUpdateDTO updateDTO) {
        return ResponseEntity.ok(enrollmentService.updateEnrollment(updateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        enrollmentService.removeUserFromCourse(id);
        return ResponseEntity.ok("Instructor removed successfully");
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<String>> listOfCourseByUser(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.findAllCoursesByUser(id));
    }
}
