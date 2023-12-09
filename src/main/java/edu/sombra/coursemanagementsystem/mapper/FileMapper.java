package edu.sombra.coursemanagementsystem.mapper;

import edu.sombra.coursemanagementsystem.dto.file.FileResponseDTO;
import edu.sombra.coursemanagementsystem.entity.File;
import org.springframework.stereotype.Component;

@Component
public class FileMapper {

    public FileResponseDTO mapToResponseDTO(File file) {
        return FileResponseDTO.builder()
                .id(file.getId())
                .name(file.getFileName())
                .build();
    }
}
