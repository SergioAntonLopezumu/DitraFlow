package com.tfg.ditraflow.model.dto;

import com.tfg.ditraflow.model.entity.*;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class AdminUserLogDTO {
    private Long id;
    private String email;
    private String companyName;
    private List<Result> results;
    private List<ChatMessage> chatHistory;
    private List<Roadmap> roadmaps;
    
    public AdminUserLogDTO() {
        this.roadmaps = new ArrayList<>();
    }
}