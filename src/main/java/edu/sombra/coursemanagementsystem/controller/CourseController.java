package edu.sombra.coursemanagementsystem.controller;

import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.LessonsByCourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.UpdateCourseDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserAssignedToCourseDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.CourseMark;
import edu.sombra.coursemanagementsystem.entity.Lesson;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/course")
public class CourseController {
    private final CourseService courseService;

    @PostMapping("/create")
    public ResponseEntity<Course> createCourse(@RequestBody CourseDTO courseDTO) {
        return ResponseEntity.ok(courseService.create(courseDTO));
    }

    //FIXME: change entity
    @PutMapping("/edit")
    public ResponseEntity<Course> editCourse(@RequestBody UpdateCourseDTO courseDTO) {
        return ResponseEntity.ok(courseService.update(courseDTO));
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

    @PutMapping("/{courseId}/{action}")
    public ResponseEntity<Course> startOrStopCourse(@PathVariable Long courseId, @PathVariable String action) {
        return ResponseEntity.ok(courseService.startOrStopCourse(courseId, action));
    }

    @GetMapping("/find-all-lessons/{id}")
    public ResponseEntity<List<Lesson>> findAllLessonsByCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.findAllLessonsByCourse(id));
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<Course>> findCoursesByInstructorId(@PathVariable Long instructorId) {
        return ResponseEntity.ok(courseService.findCoursesByInstructorId(instructorId));
    }

    @GetMapping("/instructor/{instructorId}/{courseId}")
    public ResponseEntity<List<UserAssignedToCourseDTO>> findUsersAssignedToCourseByInstructorId(@PathVariable Long instructorId,
                                                                                                 @PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.findStudentsAssignedToCourseByInstructorId(instructorId, courseId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Course>> findCoursesByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(courseService.findCoursesByUserId(studentId));
    }

    @GetMapping("/student/lessons/{studentId}/{courseId}")
    public ResponseEntity<LessonsByCourseDTO> findLessonsByCourseIdAssignedToStudentId(@PathVariable Long studentId,
                                                                                       @PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.findAllLessonsByCourseAssignedToUserId(studentId, courseId));
    }

    @GetMapping("/finish/{studentId}/{courseId}")
    public ResponseEntity<CourseMark> finishCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.finishCourse(studentId, courseId));
    }
}
