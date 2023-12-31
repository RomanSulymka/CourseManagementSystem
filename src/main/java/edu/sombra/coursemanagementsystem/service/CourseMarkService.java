package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.entity.CourseMark;

import java.util.List;

public interface CourseMarkService {
    void save(CourseMark courseMark);

    CourseMark findById(Long id);

    List<CourseMark> findAll();

    void saveTotalMark(Long userId, Long courseId, Double averageMark, Boolean isAllHomeworksGraded);

    boolean isCoursePassed(Double averageMark, boolean isAllHomeworksGraded);
}
