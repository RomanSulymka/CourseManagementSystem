package edu.sombra.coursemanagementsystem.mapper;

import edu.sombra.coursemanagementsystem.dto.course.LessonsByCourseDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.CourseFeedback;
import edu.sombra.coursemanagementsystem.entity.CourseMark;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseMapper {
    private final LessonMapper lessonMapper;

    public LessonsByCourseDTO toDTO(Course course, List<Lesson> lessons, CourseMark courseMark,
                                    Long studentId, CourseFeedback feedback) {
        return LessonsByCourseDTO.builder()
                .courseId(course.getId())
                .courseName(course.getName())
                .feedback(feedback != null ? feedback.getFeedbackText() : null)
                .totalScore(courseMark.getTotalScore())
                .passed(courseMark.getPassed())
                .lessonDTO(lessonMapper.toDTO(lessons, studentId))
                .build();
    }
}
