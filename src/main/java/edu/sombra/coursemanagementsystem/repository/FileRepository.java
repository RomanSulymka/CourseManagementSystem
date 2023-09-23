package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.File;
import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;

public interface FileRepository extends BaseRepository<File, Long> {
    byte[] findDataById(Long fileId);

    String findFileNameById(Long fileId);

    File findFileByName(String fileName);
}
