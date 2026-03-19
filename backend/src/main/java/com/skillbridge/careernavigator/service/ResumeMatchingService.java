package com.skillbridge.careernavigator.service;

import com.skillbridge.careernavigator.dto.ResumeMatchResultDto;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.entity.SkillAlias;
import com.skillbridge.careernavigator.repository.SkillAliasRepository;
import com.skillbridge.careernavigator.util.TextNormalizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeMatchingService {

    private final SkillAliasRepository skillAliasRepository;

    /**
     * Executes a 100% deterministic text matching scan across resume constraints without LLM inferences.
     * Utilizes explicit word boundary regex comparisons mapped efficiently against batch DB queries.
     */
    @Transactional(readOnly = true)
    public ResumeMatchResultDto matchSkills(String resumeText, List<Skill> topKSkills) {
        if (topKSkills == null || topKSkills.isEmpty()) {
            return new ResumeMatchResultDto(new ArrayList<>(), new ArrayList<>());
        }

        if (resumeText == null || resumeText.trim().isEmpty()) {
            return new ResumeMatchResultDto(new ArrayList<>(), new ArrayList<>(topKSkills));
        }

        // 1. Normalize Resume Text
        String normalizedResume = TextNormalizationUtils.normalizeText(resumeText);

        // 2. Performance: Fetch all aliases in a single batch query
        List<UUID> skillIds = topKSkills.stream().map(Skill::getId).collect(Collectors.toList());
        List<SkillAlias> allAliases = skillAliasRepository.findBySkillIdIn(skillIds);
        
        Map<UUID, List<String>> aliasMap = allAliases.stream()
                .collect(Collectors.groupingBy(
                        alias -> alias.getSkill().getId(),
                        Collectors.mapping(alias -> TextNormalizationUtils.normalizeText(alias.getAliasName()), Collectors.toList())
                ));

        List<Skill> presentSkills = new ArrayList<>();
        List<Skill> missingSkills = new ArrayList<>();

        // 3. Match each Skill 
        for (Skill skill : topKSkills) {
            String normalizedName = TextNormalizationUtils.normalizeText(skill.getName());
            List<String> validTargets = new ArrayList<>();
            validTargets.add(normalizedName);
            validTargets.addAll(aliasMap.getOrDefault(skill.getId(), new ArrayList<>()));

            boolean isFound = false;
            for (String target : validTargets) {
                String boundaryRegex = TextNormalizationUtils.buildBoundaryRegex(target);
                Pattern pattern = Pattern.compile(boundaryRegex, Pattern.CASE_INSENSITIVE);
                
                if (pattern.matcher(normalizedResume).find()) {
                    isFound = true;
                    break;
                }
            }

            if (isFound) {
                presentSkills.add(skill);
            } else {
                missingSkills.add(skill);
            }
        }

        return ResumeMatchResultDto.builder()
                .presentSkills(presentSkills)
                .missingSkills(missingSkills)
                .build();
    }
}
