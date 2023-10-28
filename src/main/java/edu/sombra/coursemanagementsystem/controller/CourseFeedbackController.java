package edu.sombra.coursemanagementsystem.controller;

import edu.sombra.coursemanagementsystem.dto.feedback.CourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.dto.feedback.GetCourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.service.CourseFeedbackService;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/feedback")
public class CourseFeedbackController {

    private final CourseFeedbackService courseFeedbackService;

    @PostMapping
    public ResponseEntity<String> addFeedback(@RequestBody CourseFeedbackDTO courseFeedbackDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(courseFeedbackService.create(courseFeedbackDTO, userDetails.getUsername()));
    }

    @PutMapping
    public ResponseEntity<GetCourseFeedbackDTO> editFeedback(@RequestBody CourseFeedbackDTO courseFeedbackDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(courseFeedbackService.edit(courseFeedbackDTO, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<GetCourseFeedbackDTO>> getAllFeedbacks() {
        return ResponseEntity.ok(courseFeedbackService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetCourseFeedbackDTO> getFeedbackById(@PathVariable Long id) {
        return ResponseEntity.ok(courseFeedbackService.findCourseFeedbackById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFeedback(@PathVariable Long id) {
        return ResponseEntity.ok(courseFeedbackService.delete(id));
    }
}
