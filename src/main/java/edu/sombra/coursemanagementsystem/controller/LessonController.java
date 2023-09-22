package edu.sombra.coursemanagementsystem.controller;

import edu.sombra.coursemanagementsystem.dto.lesson.CreateLessonDTO;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.service.LessonService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/lesson")
public class LessonController {
    private final LessonService lessonService;

    //FIXME: створювати автоматично 5 уроків для курсу, коли він тільки створюється
    @PostMapping("/create")
    public ResponseEntity<Lesson> create(@RequestBody CreateLessonDTO lessonDTO) {
        return ResponseEntity.ok(lessonService.save(lessonDTO));
    }


}
