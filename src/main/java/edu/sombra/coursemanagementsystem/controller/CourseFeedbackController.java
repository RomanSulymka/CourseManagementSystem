package edu.sombra.coursemanagementsystem.controller;

import edu.sombra.coursemanagementsystem.dto.feedback.CourseFeedbackDTO;
import edu.sombra.coursemanagementsystem.service.CourseFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/feedback")
public class CourseFeedbackController {

    private final CourseFeedbackService courseFeedbackService;

    @PostMapping
    public ResponseEntity<String> addFeedback(@RequestBody CourseFeedbackDTO courseFeedbackDTO) {
        return ResponseEntity.ok(courseFeedbackService.create(courseFeedbackDTO));
    }
}
