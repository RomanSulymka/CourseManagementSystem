package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseMarkResponseDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.course.LessonsByCourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.UpdateCourseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.LessonResponseDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserAssignedToCourseDTO;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;

import java.util.List;

public interface CourseService {
    CourseResponseDTO create(CourseDTO courseDTO);

    CourseResponseDTO findByName(String courseName);

    CourseResponseDTO update(UpdateCourseDTO course);

    CourseResponseDTO findById(Long courseId);

    void delete(Long id);

    List<CourseResponseDTO> findAllCourses();

    CourseResponseDTO updateStatus(Long id, CourseStatus status);

    List<LessonResponseDTO> findAllLessonsByCourse(Long id);

    CourseResponseDTO findCourseByHomeworkId(Long userId, Long homeworkId);

    List<CourseResponseDTO> findCoursesByUserId(Long instructorId, String userEmail);

    List<UserAssignedToCourseDTO> findStudentsAssignedToCourseByInstructorId(Long instructorId, Long courseId);

    LessonsByCourseDTO findAllLessonsByCourseAssignedToUserId(Long studentId, Long courseId);

    CourseMarkResponseDTO finishCourse(Long studentId, Long courseId);

    CourseResponseDTO startOrStopCourse(Long courseId, String action);
}
