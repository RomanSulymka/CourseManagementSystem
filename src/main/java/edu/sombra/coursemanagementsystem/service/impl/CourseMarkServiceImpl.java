package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.CourseMark;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.repository.CourseMarkRepository;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.CourseMarkService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class CourseMarkServiceImpl implements CourseMarkService {
    public static final String TOTAL_MARK_SAVED_SUCCESSFULLY = "Total mark saved successfully";
    public static final int MIN_PASS_MARK = 80;
    private final CourseMarkRepository courseMarkRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

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
        User user = userRepository.findById(userId).orElseThrow();
        Course course = courseRepository.findById(courseId).orElseThrow();
        courseMarkRepository.upsert(CourseMark.builder()
                .user(user)
                .course(course)
                .totalScore(averageMark != null ? BigDecimal.valueOf(averageMark) : null)
                .passed(isCoursePassed(averageMark, isAllHomeworksGraded))
                .build());
        log.info(TOTAL_MARK_SAVED_SUCCESSFULLY);
    }

    @Override
    public boolean isCoursePassed(Double averageMark, boolean isAllHomeworksGraded) {
        return isAllHomeworksGraded && averageMark >= MIN_PASS_MARK;
    }
}
