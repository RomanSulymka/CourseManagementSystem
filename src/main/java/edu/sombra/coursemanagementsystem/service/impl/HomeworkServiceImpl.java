package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.homework.HomeworkDTO;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import edu.sombra.coursemanagementsystem.service.HomeworkService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AllArgsConstructor
@Slf4j
@Service
public class HomeworkServiceImpl implements HomeworkService {
    private final HomeworkRepository homeworkRepository;

    @Override
    public void save(Homework homework) {
        homeworkRepository.save(homework);
        log.info("Homework saved successfully");
    }

    @Override
    public void setMark(Long homeworkId, Long mark) {
        try {
            if (mark >= 0 && mark <= 100) {
                homeworkRepository.setMark(homeworkId, mark);
                log.info("Mark saved successfully");
            } else {
                log.error("Invalid mark value. Mark should be between 0 and 100., But now mark is {}", mark);
                throw new IllegalArgumentException("Invalid mark value. Mark should be between 0 and 100.");
            }
        } catch (DataAccessException e) {
            throw new IllegalArgumentException("Invalid mark value. Mark should be between 0 and 100.");
        }
    }
}
