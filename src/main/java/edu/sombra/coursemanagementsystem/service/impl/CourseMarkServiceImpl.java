package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.CourseMark;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.repository.CourseMarkRepository;
import edu.sombra.coursemanagementsystem.service.CourseMarkService;
import edu.sombra.coursemanagementsystem.service.CourseService;
import edu.sombra.coursemanagementsystem.service.UserService;
import jakarta.persistence.EntityNotFoundException;
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
    public void saveTotalMark(Long userId, Long courseId, Double averageMark) {
        User user = userService.findUserById(userId);
        Course course = courseService.findById(courseId);
        if (!isTotalMarkExist(userId, courseId)) {
            courseMarkRepository.save(CourseMark.builder()
                    .user(user)
                    .course(course)
                    .totalScore(BigDecimal.valueOf(averageMark))
                    .build());
            log.info("Total mark saved successfully");
        } else {
            CourseMark courseMark = findCourseByUserIdAndCourseId(userId, courseId)
                    .orElseThrow(EntityNotFoundException::new);
            courseMarkRepository.update(CourseMark.builder()
                    .id(courseMark.getId())
                    .user(user)
                    .course(course)
                    .totalScore(BigDecimal.valueOf(averageMark))
                    .build());
            log.info("Total mark updated successfully");
        }
    }

    private boolean isTotalMarkExist(Long userId, Long courseId) {
        return findCourseByUserIdAndCourseId(userId, courseId).isPresent();
    }

    private Optional<CourseMark> findCourseByUserIdAndCourseId(Long userId, Long courseId) {
        return courseMarkRepository.findCourseMarkByUserIdAndCourseId(userId, courseId);
    }
}
