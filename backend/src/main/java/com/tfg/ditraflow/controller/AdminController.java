package com.tfg.ditraflow.controller;

import com.tfg.ditraflow.model.dto.AdminUserLogDTO;
import com.tfg.ditraflow.repository.*;
import com.tfg.ditraflow.model.entity.User;
import com.tfg.ditraflow.model.entity.Result;
import com.tfg.ditraflow.model.entity.Roadmap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private IUserRepository userRepository;
    @Autowired private IResultRepository resultRepository;
    @Autowired private IChatMessageRepository chatHistoryRepository;

    @GetMapping("/dashboard-data")
    public ResponseEntity<?> getGlobalStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalResults", resultRepository.count());
        stats.put("totalChats", chatHistoryRepository.count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users-logs")
    public ResponseEntity<List<AdminUserLogDTO>> getAllUsersWithDetails() {
        List<User> users = userRepository.findAll();
        List<AdminUserLogDTO> logs = users.stream().map(user -> {
            AdminUserLogDTO dto = new AdminUserLogDTO();
            dto.setId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setCompanyName(user.getCompanyName());
            List<Result> results = resultRepository.findByUser(user);
            dto.setResults(results);
            dto.setChatHistory(chatHistoryRepository.findByUserOrderByTimestampDesc(user));
            
            List<Roadmap> roadmaps = new ArrayList<>();
            for (Result result : results) {
                if (result.getRoadmap() != null) {
                    roadmaps.add(result.getRoadmap());
                }
            }
            dto.setRoadmaps(roadmaps);
            
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(logs);
    }

    @DeleteMapping("/user/{userId}")
    @Transactional
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }
        userRepository.deleteById(userId);
        return ResponseEntity.ok("Usuario eliminado exitosamente");
    }

    @DeleteMapping("/user/{userId}/data")
    @Transactional
    public ResponseEntity<String> deleteUserData(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }
        User user = userOpt.get();
        resultRepository.deleteAll(resultRepository.findByUser(user));
        chatHistoryRepository.deleteAll(chatHistoryRepository.findByUserOrderByTimestampDesc(user));
        return ResponseEntity.ok("Datos del usuario eliminados exitosamente");
    }

    @GetMapping("/user/{userId}/details")
    public ResponseEntity<?> getUserDetails(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }
        User user = userOpt.get();
        AdminUserLogDTO dto = new AdminUserLogDTO();
        dto.setEmail(user.getEmail());
        dto.setCompanyName(user.getCompanyName());
        dto.setResults(resultRepository.findByUser(user));
        dto.setChatHistory(chatHistoryRepository.findByUserOrderByTimestampDesc(user));
        
        List<Roadmap> roadmaps = new ArrayList<>();
        for (Result result : dto.getResults()) {
            if (result.getRoadmap() != null) {
                roadmaps.add(result.getRoadmap());
            }
        }
        dto.setRoadmaps(roadmaps);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/database/reset")
    @Transactional
    public ResponseEntity<String> resetDatabase() {
        userRepository.deleteAll();
        return ResponseEntity.ok("Base de datos reseteada exitosamente.");
    }
}