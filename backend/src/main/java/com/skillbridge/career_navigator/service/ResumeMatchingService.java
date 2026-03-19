package com.skillbridge.career_navigator.service;

import com.skillbridge.career_navigator.dto.ResumeMatchResultDto;
import com.skillbridge.career_navigator.entity.Skill;
import com.skillbridge.career_navigator.entity.SkillAlias;
import com.skillbridge.career_navigator.repository.SkillAliasRepository;
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

        // 1. Normalize Resume Text (lowercase, collapse excessive spaces to standard, strip most punctuation but preserve symbols for things like C# or C++)
        String normalizedResume = normalizeText(resumeText);

        // 2. Performance: Fetch all aliases in a single batch query to prevent N+1 cascades
        List<UUID> skillIds = topKSkills.stream().map(Skill::getId).collect(Collectors.toList());
        List<SkillAlias> allAliases = skillAliasRepository.findBySkillIdIn(skillIds);
        
        // Group aliases by Skill ID in memory for O(1) loop retrieval
        Map<UUID, List<String>> aliasMap = allAliases.stream()
                .collect(Collectors.groupingBy(
                        alias -> alias.getSkill().getId(),
                        Collectors.mapping(alias -> normalizeText(alias.getAliasName()), Collectors.toList())
                ));

        List<Skill> presentSkills = new ArrayList<>();
        List<Skill> missingSkills = new ArrayList<>();

        // 3. Match each Skill exactly using RegExp word boundaries to avoid false substring overlaps (e.g. "Java" matching in "JavaScript")
        for (Skill skill : topKSkills) {
            String normalizedName = normalizeText(skill.getName());
            List<String> validTargets = new ArrayList<>();
            validTargets.add(normalizedName);
            validTargets.addAll(aliasMap.getOrDefault(skill.getId(), new ArrayList<>()));

            boolean isFound = false;
            for (String target : validTargets) {
                // Compile boundary checking explicitly. Pattern quotes ensure symbols like "+" or "#" parse natively.
                String boundaryRegex = "\\b" + Pattern.quote(target) + "\\b";
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

    /**
     * Formatting normalizer. Trims and handles spacing dynamically without destroying core programming characters (#, +).
     */
    private String normalizeText(String input) {
        if (input == null) return "";
        // Removes punctuation except for #, +, and . which are critical to IT skills
        return input.toLowerCase()
                .replaceAll("[^a-z0-9#\\+\\.\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
