package edu.sombra.coursemanagementsystem.mapper;

import edu.sombra.coursemanagementsystem.dto.homework.HomeworkWithFileDTO;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HomeworkMapper {
    private final FileRepository fileRepository;

    public HomeworkWithFileDTO toDTO(Homework homework) {
        return HomeworkWithFileDTO.builder()
                .homeworkId(homework.getId())
                .mark(homework.getMark())
                .fileName(fileRepository.findFileNameById(homework.getFile().getId()))
                .build();
    }
}
