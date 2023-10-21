package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.CourseFeedback;
import edu.sombra.coursemanagementsystem.repository.CourseFeedbackRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CourseFeedbackRepositoryImpl implements CourseFeedbackRepository {
    @Getter
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Class<CourseFeedback> getEntityClass() {
        return CourseFeedback.class;
    }
}
