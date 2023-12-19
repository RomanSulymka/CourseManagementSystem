package edu.sombra.coursemanagementsystem.controller;

import edu.sombra.coursemanagementsystem.dto.course.CourseActionDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseAssignedToUserDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseMarkResponseDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.course.LessonsByCourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.UpdateCourseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.LessonResponseDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserAssignedToCourseDTO;
import edu.sombra.coursemanagementsystem.service.CourseService;
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
@RequestMapping("/api/v1/course")
public class CourseController {
    private final CourseService courseService;

    @PostMapping("/create")
    public ResponseEntity<CourseResponseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        return ResponseEntity.ok(courseService.create(courseDTO));
    }

    @PutMapping("/edit")
    public ResponseEntity<CourseResponseDTO> editCourse(@RequestBody UpdateCourseDTO courseDTO) {
        return ResponseEntity.ok(courseService.update(courseDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.findById(id));
    }

    @GetMapping("/find-all")
    public ResponseEntity<List<CourseResponseDTO>> findAll() {
        return ResponseEntity.ok(courseService.findAllCourses());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity<CourseResponseDTO> startOrStopCourse(@RequestBody CourseActionDTO courseActionDTO) {
        return ResponseEntity.ok(courseService.startOrStopCourse(courseActionDTO.getCourseId(), courseActionDTO.getAction()));
    }

    @GetMapping("/find-all-lessons/{id}")
    public ResponseEntity<List<LessonResponseDTO>> findAllLessonsByCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.findAllLessonsByCourse(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CourseResponseDTO>> findCoursesByUserId(@PathVariable Long userId,
                                                                       @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(courseService.findCoursesByUserId(userId, userDetails.getUsername()));
    }

    @PostMapping("/instructor/users")
    public ResponseEntity<List<UserAssignedToCourseDTO>> findUsersAssignedToCourseByInstructorId(@RequestBody CourseAssignedToUserDTO
                                                                                                             courseAssignedToUserDTO) {
        return ResponseEntity.ok(courseService.findStudentsAssignedToCourseByInstructorId(courseAssignedToUserDTO.getUserId(), courseAssignedToUserDTO.getCourseId()));
    }

    @PostMapping("/student/lessons")
    public ResponseEntity<LessonsByCourseDTO> findLessonsByCourseIdAssignedToStudentId(@RequestBody CourseAssignedToUserDTO
                                                                                               courseAssignedToUserDTO) {
        return ResponseEntity.ok(courseService.findAllLessonsByCourseAssignedToUserId(courseAssignedToUserDTO.getUserId(),
                courseAssignedToUserDTO.getCourseId()));
    }

    @PostMapping("/finish")
    public ResponseEntity<CourseMarkResponseDTO> finishCourse(@RequestBody CourseAssignedToUserDTO
                                                                          courseAssignedToUserDTO) {
        return ResponseEntity.ok(courseService.finishCourse(courseAssignedToUserDTO.getUserId(), courseAssignedToUserDTO.getUserId()));
    }
}
