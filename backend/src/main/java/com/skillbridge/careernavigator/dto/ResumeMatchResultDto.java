package com.skillbridge.careernavigator.dto;

import com.skillbridge.careernavigator.entity.Skill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeMatchResultDto {
    private List<Skill> presentSkills;
    private List<Skill> missingSkills;
}
