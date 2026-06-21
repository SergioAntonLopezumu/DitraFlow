package com.tfg.ditraflow.repository;

import com.tfg.ditraflow.model.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface IRoadmapRepository extends JpaRepository<Roadmap, Long> {
    Optional<Roadmap> findByResultId(Long resultId);
    
    // Obtener roadmap más reciente para un resultado (ordenado por fecha descendente)
    @Query("SELECT r FROM Roadmap r WHERE r.result.id = :resultId ORDER BY r.generatedAt DESC LIMIT 1")
    Optional<Roadmap> findLatestByResultId(@Param("resultId") Long resultId);
    
    // Obtener todos los roadmaps para un resultado
    List<Roadmap> findByResultIdOrderByGeneratedAtDesc(Long resultId);
}