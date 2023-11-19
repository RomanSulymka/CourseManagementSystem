package edu.sombra.coursemanagementsystem.controller;

import edu.sombra.coursemanagementsystem.dto.lesson.CreateLessonDTO;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.service.LessonService;
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
@RequestMapping("/api/v1/lesson")
public class LessonController {
    private final LessonService lessonService;

    @PostMapping("/create")
    public ResponseEntity<Lesson> create(@RequestBody CreateLessonDTO lessonDTO) {
        return ResponseEntity.ok(lessonService.save(lessonDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.ok("Lesson deleted successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.findById(id));
    }

    @GetMapping("/find-all")
    public ResponseEntity<List<Lesson>> getAllLessons() {
        return ResponseEntity.ok(lessonService.findAllLessons());
    }

    @GetMapping("/find-all/{id}")
    public ResponseEntity<List<Lesson>> getAllLessonsByCourseId(@PathVariable("id") Long courseId) {
        return ResponseEntity.ok(lessonService.findAllLessonsByCourse(courseId));
    }

    @PutMapping("/edit")
    public ResponseEntity<Lesson> editLesson(@RequestBody Lesson lesson) {
        return ResponseEntity.ok(lessonService.editLesson(lesson));
    }
}
