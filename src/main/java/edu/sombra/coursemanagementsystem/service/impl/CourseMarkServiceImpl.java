package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.CourseMark;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.repository.CourseMarkRepository;
import edu.sombra.coursemanagementsystem.service.CourseMarkService;
import edu.sombra.coursemanagementsystem.service.CourseService;
import edu.sombra.coursemanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class CourseMarkServiceImpl implements CourseMarkService {
    private final CourseMarkRepository courseMarkRepository;
    private final CourseService courseService;
    private final UserService userService;

    @Override
    public void save(CourseMark courseMark) {
        courseMarkRepository.save(courseMark);
    }

    @Override
    public CourseMark findById(Long id) {
        return courseMarkRepository.findById(id).orElseThrow();
    }

    @Override
    public List<CourseMark> findAll() {
        return courseMarkRepository.findAll();
    }

    @Override
    public void saveTotalMark(Long userId, Long courseId, Double averageMark, Boolean isAllHomeworksGraded) {
        User user = userService.findUserById(userId);
        Course course = courseService.findById(courseId);
        courseMarkRepository.upsert(CourseMark.builder()
                .user(user)
                .course(course)
                .totalScore(BigDecimal.valueOf(averageMark))
                .passed(isCoursePassed(averageMark, isAllHomeworksGraded))
                .build());
        log.info("Total mark saved successfully");
    }

    private boolean isCoursePassed(Double averageMark, boolean isAllHomeworksGraded) {
        if (isAllHomeworksGraded) {
            return averageMark >= 80;
        } else {
            return false;
        }
    }

    private boolean isTotalMarkExist(Long userId, Long courseId) {
        return findCourseMarkByUserIdAndCourseId(userId, courseId).isPresent();
    }

    private Optional<CourseMark> findCourseMarkByUserIdAndCourseId(Long userId, Long courseId) {
        return courseMarkRepository.findCourseMarkByUserIdAndCourseId(userId, courseId);
    }
}
