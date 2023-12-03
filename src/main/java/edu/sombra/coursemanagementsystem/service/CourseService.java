package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.course.LessonsByCourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.UpdateCourseDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserAssignedToCourseDTO;
import edu.sombra.coursemanagementsystem.entity.CourseMark;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;

import java.util.List;

public interface CourseService {
    CourseResponseDTO create(CourseDTO courseDTO);

    CourseResponseDTO findByName(String courseName);

    CourseResponseDTO update(UpdateCourseDTO course);

    CourseResponseDTO findById(Long courseId);

    boolean delete(Long id);

    List<CourseResponseDTO> findAllCourses();

    CourseResponseDTO updateStatus(Long id, CourseStatus status);

    List<Lesson> findAllLessonsByCourse(Long id);

    CourseResponseDTO findCourseByHomeworkId(Long userId, Long homeworkId);

    List<CourseResponseDTO> findCoursesByInstructorId(Long instructorId);

    List<UserAssignedToCourseDTO> findStudentsAssignedToCourseByInstructorId(Long instructorId, Long courseId);

    List<CourseResponseDTO> findCoursesByUserId(Long userId);

    LessonsByCourseDTO findAllLessonsByCourseAssignedToUserId(Long studentId, Long courseId);

    CourseMark finishCourse(Long studentId, Long courseId);

    CourseResponseDTO startOrStopCourse(Long courseId, String action);
}
