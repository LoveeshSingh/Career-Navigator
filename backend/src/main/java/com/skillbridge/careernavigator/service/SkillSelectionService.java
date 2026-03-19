package com.skillbridge.careernavigator.service;

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
     * Extracts skills for the dynamic JD flow prioritized by deterministic parsing scores Top K.
     */
    public List<Skill> selectSkillsForJd(List<com.skillbridge.careernavigator.dto.ParsedSkillDto> parsedSkills, int topK) {
        if (parsedSkills == null || parsedSkills.isEmpty()) {
            return Collections.emptyList();
        }

        // Sort by computed rule-based scores DESC
        List<Skill> availableSkills = parsedSkills.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .map(com.skillbridge.careernavigator.dto.ParsedSkillDto::getSkill)
                .distinct()
                .collect(Collectors.toList());

        int k = Math.min(topK, availableSkills.size());
        return availableSkills.subList(0, k);
    }
}
