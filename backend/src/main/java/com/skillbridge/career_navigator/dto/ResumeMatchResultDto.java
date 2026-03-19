package com.skillbridge.career_navigator.dto;

import com.skillbridge.career_navigator.entity.Skill;
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
