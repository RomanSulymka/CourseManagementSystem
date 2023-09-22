package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.exception.LessonException;
import edu.sombra.coursemanagementsystem.repository.LessonRepository;
import edu.sombra.coursemanagementsystem.service.LessonService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;

    @Override
    public Lesson save(Lesson lesson) {
        try {
            return lessonRepository.save(lesson);
        } catch (DataAccessException ex) {
            log.error("Error creating lesson: {}", ex.getMessage(), ex);
            throw new LessonException("Failed to create lesson", ex);
        }
    }
}
