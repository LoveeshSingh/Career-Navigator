package com.skillbridge.careernavigator.dto;

import com.skillbridge.careernavigator.entity.Skill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidatedSkillDto {
    private Skill skill;
    private Double importanceScore;
}
