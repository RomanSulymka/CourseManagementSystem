package edu.sombra.coursemanagementsystem.mapper;

import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkDTO;
import edu.sombra.coursemanagementsystem.dto.homework.HomeworkWithFileDTO;
import edu.sombra.coursemanagementsystem.entity.Homework;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HomeworkMapper {

    public HomeworkWithFileDTO toDTO(Homework homework) {
        return HomeworkWithFileDTO.builder()
                .homeworkId(homework.getId())
                .mark(homework.getMark())
                .fileName(homework.getFile() != null ? homework.getFile().getFileName() : null)
                .build();
    }

    public List<GetHomeworkDTO> mapToDTO(List<Homework> homeworks) {
        return homeworks.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public GetHomeworkDTO mapToDTO(Homework homework) {
        return GetHomeworkDTO.builder()
                .id(homework.getId())
                .mark(homework.getMark())
                .fileName(homework.getFile() != null ? homework.getFile().getFileName() : null)
                .lesson(homework.getLesson())
                .userEmail(homework.getUser().getEmail())
                .userId(homework.getUser().getId())
                .build();
    }
}
