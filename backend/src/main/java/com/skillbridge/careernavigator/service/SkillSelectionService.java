package com.skillbridge.careernavigator.service;

import com.skillbridge.careernavigator.dto.ValidatedSkillDto;
import com.skillbridge.careernavigator.entity.Role;
import com.skillbridge.careernavigator.entity.RoleSkills;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillSelectionService {

    private final RoleRepository roleRepository;

    /**
     * Extracts skills for a predefined role, bounded by Top K limits.
     * Respects the database's native sorting (RoleSkills priority).
     */
    @Transactional(readOnly = true)
    public List<Skill> selectSkillsForRole(UUID roleId, int topK) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found in registry"));

        // roleSkills are already sorted natively via @OrderBy("priority ASC")
        List<Skill> availableSkills = role.getRoleSkills().stream()
                .map(RoleSkills::getSkill)
                .distinct()
                .limit(20) // Strict cap at 20 predefined skills.
                .collect(Collectors.toList());

        int k = Math.min(topK, availableSkills.size());
        return availableSkills.subList(0, k);
    }

    /**
     * Extracts skills for the dynamic JD flow sequentially prioritized by NLP importance bounding Top K.
     */
    public List<Skill> selectSkillsForJd(List<ValidatedSkillDto> validatedSkills, int topK) {
        if (validatedSkills == null || validatedSkills.isEmpty()) {
            return Collections.emptyList();
        }

        // Sort dynamically by NLP exact scores DESC descending into Top K truncation
        List<Skill> availableSkills = validatedSkills.stream()
                .sorted((a, b) -> Double.compare(b.getImportanceScore(), a.getImportanceScore()))
                .map(ValidatedSkillDto::getSkill)
                .distinct()
                .collect(Collectors.toList());

        int k = Math.min(topK, availableSkills.size());
        return availableSkills.subList(0, k);
    }
}
