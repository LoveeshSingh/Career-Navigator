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
public class ParsedSkillDto {
    private Skill skill;
    private double score;
}
