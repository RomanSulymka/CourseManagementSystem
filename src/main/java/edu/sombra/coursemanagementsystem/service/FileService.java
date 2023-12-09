package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.file.FileResponseDTO;
import edu.sombra.coursemanagementsystem.entity.File;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    FileResponseDTO saveFile(MultipartFile file, Long lessonId, Long userId) throws IOException;

    File getFileDataById(Long fileId);

    Resource downloadFile(Long fileId);

    void delete(Long fileId, String userEmail);
}
