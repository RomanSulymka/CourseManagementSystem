package edu.sombra.coursemanagementsystem.controller;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.service.CourseService;
import lombok.RequiredArgsConstructor;
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
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/course")
public class CourseController {
    private final CourseService courseService;

    @PostMapping("/create")
    public ResponseEntity<String> createCourse(@RequestBody Course course) {
        return ResponseEntity.ok(courseService.create(course));
    }

    @PutMapping("/edit")
    public ResponseEntity<Course> editCourse(@RequestBody Course course) {
        return ResponseEntity.ok(courseService.update(course));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.findById(id));
    }

    @GetMapping("/find-all")
    public ResponseEntity<List<Course>> findAll() {
        return ResponseEntity.ok(courseService.findAllCourses());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.delete(id));
    }
}
