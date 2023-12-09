package edu.sombra.coursemanagementsystem.dto.file;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileResponseDTO {
    private Long id;
    private String name;
}
