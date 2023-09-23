package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.entity.File;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    void saveFile(MultipartFile file, Long lessonId, Long userId) throws IOException;

    File getFileDataById(Long fileId);

    String getFileNameById(Long fileId);

    Resource downloadFile(Long fileId);
}
