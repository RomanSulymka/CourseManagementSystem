package edu.sombra.coursemanagementsystem.controller;

import edu.sombra.coursemanagementsystem.dto.homework.HomeworkDTO;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.service.HomeworkService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/homework")
public class HomeworkController {
    private final HomeworkService homeworkService;

    @PutMapping("/mark")
    public ResponseEntity<String> setMark(@RequestBody HomeworkDTO homeworkDTO) {
        homeworkService.setMark(homeworkDTO.getHomeworkId(), homeworkDTO.getMark());
        return ResponseEntity.ok("Mark saved successfully");
    }
}
