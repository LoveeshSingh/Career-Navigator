package com.skillbridge.careernavigator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoadmapResponseDto {
    private String mode;
    private String message;
    private List<String> presentSkills;
    private List<String> missingSkills;
    private Double matchPercentage;
    private Object data;
}
