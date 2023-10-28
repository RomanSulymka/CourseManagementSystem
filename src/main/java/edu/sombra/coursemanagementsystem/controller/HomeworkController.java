package edu.sombra.coursemanagementsystem.controller;

import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkDTO;
import edu.sombra.coursemanagementsystem.dto.homework.HomeworkDTO;
import edu.sombra.coursemanagementsystem.service.HomeworkService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/homework")
public class HomeworkController {
    private final HomeworkService homeworkService;

    @PutMapping("/mark")
    public ResponseEntity<String> setMark(@RequestBody HomeworkDTO homeworkDTO) {
        homeworkService.setMark(homeworkDTO.getUserId(), homeworkDTO.getHomeworkId(), homeworkDTO.getMark());
        return ResponseEntity.ok("Mark saved successfully");
    }

    @GetMapping("/{homeworkId}")
    public ResponseEntity<GetHomeworkDTO> getHomework(@PathVariable Long homeworkId) {
        return ResponseEntity.ok(homeworkService.findHomeworkById(homeworkId));
    }

    @GetMapping
    public ResponseEntity<List<GetHomeworkDTO>> getAllHomeworks() {
        return ResponseEntity.ok(homeworkService.getAllHomeworks());
    }

    @DeleteMapping("/{homeworkId}")
    public ResponseEntity<String> delete(@PathVariable Long homeworkId) {
        return ResponseEntity.ok(homeworkService.deleteHomework(homeworkId));
    }
}
