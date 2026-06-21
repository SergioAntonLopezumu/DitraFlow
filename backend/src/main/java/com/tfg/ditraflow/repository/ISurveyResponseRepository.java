package com.tfg.ditraflow.repository;

import com.tfg.ditraflow.model.entity.SurveyResponse;
import com.tfg.ditraflow.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ISurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    List<SurveyResponse> findByUser(User user);
    Optional<SurveyResponse> findByUserAndId(User user, Long id);
}
