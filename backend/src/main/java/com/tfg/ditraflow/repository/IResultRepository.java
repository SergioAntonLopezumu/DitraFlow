package com.tfg.ditraflow.repository;

import com.tfg.ditraflow.model.entity.Result;
import com.tfg.ditraflow.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByUserId(Long userId);
    List<Result> findByUser(User user);
    List<Result> findByUserOrderByCreatedAtDesc(User user);
    Optional<Result> findFirstByUserOrderByCreatedAtDesc(User user);
}