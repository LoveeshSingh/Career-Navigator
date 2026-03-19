package com.skillbridge.career_navigator.dto;

import com.skillbridge.career_navigator.entity.Skill;
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
