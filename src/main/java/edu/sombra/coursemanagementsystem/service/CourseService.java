package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.LessonsByCourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.UpdateCourseDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserAssignedToCourseDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.CourseMark;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.enums.CourseStatus;

import java.util.List;

public interface CourseService {
    Course create(CourseDTO courseDTO);

    Course findByName(String courseName);

    Course update(UpdateCourseDTO course);

    Course findById(Long courseId);

    boolean delete(Long id);

    List<Course> findAllCourses();

    Course updateStatus(Long id, CourseStatus status);

    List<Lesson> findAllLessonsByCourse(Long id);

    Course findCourseByHomeworkId(Long userId, Long homeworkId);

    List<Course> findCoursesByInstructorId(Long instructorId);

    List<UserAssignedToCourseDTO> findStudentsAssignedToCourseByInstructorId(Long instructorId, Long courseId);

    List<Course> findCoursesByUserId(Long userId);

    LessonsByCourseDTO findAllLessonsByCourseAssignedToUserId(Long studentId, Long courseId);

    CourseMark finishCourse(Long studentId, Long courseId);

    Course startOrStopCourse(Long courseId, String action);
}
