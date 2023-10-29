package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.File;
import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;

public interface FileRepository extends BaseRepository<File, Long> {
    String findFileNameById(Long fileId);
}
