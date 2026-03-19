package com.skillbridge.career_navigator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapRequestDto {
    private UUID roleId;
    private String jdText;
    private String resumeText;
    private String level; // beginner, intermediate
    private Integer hoursPerWeek;
    private Integer topK;
}
