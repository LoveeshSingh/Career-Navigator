package com.skillbridge.careernavigator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapRequestDto {
    private UUID roleId;
    private String role;
    private String jdText;

    @NotBlank(message = "Resume text is explicitly required")
    private String resumeText;

    @Pattern(regexp = "^(beginner|intermediate|advanced)$", message = "Level must be beginner, intermediate, or advanced")
    private String level; 

    @Min(value = 1, message = "Hours per week must be at least 1")
    private Integer hoursPerWeek;

    @Min(value = 1, message = "Top K must be at least 1")
    @Max(value = 20, message = "Top K cannot exceed 20")
    private Integer topK;
}
