package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.entity.Homework;

public interface HomeworkService {

    void save(Homework homework);

    void setMark(Long userId, Long homeworkId, Long mark);

    Homework findByUserAndLessonId(Long userId, Long lessonId);
}
