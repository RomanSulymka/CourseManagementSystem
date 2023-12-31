package edu.sombra.coursemanagementsystem.mapper;

import edu.sombra.coursemanagementsystem.dto.course.CourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.course.LessonsByCourseDTO;
import edu.sombra.coursemanagementsystem.dto.course.UpdateCourseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.LessonDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.CourseFeedback;
import edu.sombra.coursemanagementsystem.entity.CourseMark;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CourseMapper {

    public LessonsByCourseDTO toDTO(Course course, CourseMark courseMark,
                                    CourseFeedback feedback, List<LessonDTO> lessonDTO) {
        return LessonsByCourseDTO.builder()
                .courseId(course.getId())
                .courseName(course.getName())
                .feedback(feedback != null ? feedback.getFeedbackText() : null)
                .totalScore(courseMark != null ? courseMark.getTotalScore() : null)
                .passed(Optional.ofNullable(courseMark).map(CourseMark::getPassed).orElse(false))
                .lessonDTO(lessonDTO)
                .build();
    }

    public Course fromDTO(UpdateCourseDTO courseDTO) {
        return Course.builder()
                .id(courseDTO.getId())
                .name(courseDTO.getName())
                .startDate(courseDTO.getStartDate())
                .status(courseDTO.getStatus())
                .build();
    }

    public Course fromCourseDTO(CourseDTO courseDTO) {
        return Course.builder()
                .name(courseDTO.getName())
                .startDate(courseDTO.getStartDate())
                .status(courseDTO.getStatus())
                .build();
    }

    public CourseResponseDTO mapToResponseDTO(Course course) {
        return CourseResponseDTO.builder()
                .courseId(course.getId())
                .courseName(course.getName())
                .startDate(course.getStartDate())
                .status(course.getStatus())
                .build();
    }

    public List<CourseResponseDTO> mapToResponsesDTO(List<Course> courseList) {
        return courseList.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public Course fromResponseDTO(CourseResponseDTO courseResponseDTO) {
        return Course.builder()
                .id(courseResponseDTO.getCourseId())
                .name(courseResponseDTO.getCourseName())
                .startDate(courseResponseDTO.getStartDate())
                .build();
    }
}
