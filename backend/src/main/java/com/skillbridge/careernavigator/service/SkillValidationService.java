package com.skillbridge.careernavigator.service;

import com.skillbridge.careernavigator.dto.ExtractedSkillDto;
import com.skillbridge.careernavigator.dto.ValidatedSkillDto;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.entity.SkillAlias;
import com.skillbridge.careernavigator.repository.SkillAliasRepository;
import com.skillbridge.careernavigator.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SkillValidationService {

    private final SkillRepository skillRepository;
    private final SkillAliasRepository skillAliasRepository;

    /**
     * Filters and maps extracted skills against the canonical PostgreSQL registry.
     * Preserves input order/ranking. Silently drops skills not found.
     *
     * @param extractedSkills The raw NLP outputs sorted by importance.
     * @return List of ValidatedSkillDto mapped to DB entities.
     */
    @Transactional(readOnly = true)
    public List<ValidatedSkillDto> validateSkills(List<ExtractedSkillDto> extractedSkills) {
        if (extractedSkills == null || extractedSkills.isEmpty()) {
            return new ArrayList<>();
        }

        List<ValidatedSkillDto> validSkills = new ArrayList<>();
        Set<String> uniqueCanonicalNames = new HashSet<>();

        for (ExtractedSkillDto dto : extractedSkills) {
            String normalizedTerm = normalizeText(dto.getSkillName());
            if (normalizedTerm.isEmpty()) continue;

            Optional<Skill> matchedSkillOpt = findSkillInDatabase(normalizedTerm);

            if (matchedSkillOpt.isPresent()) {
                Skill canonicalSkill = matchedSkillOpt.get();
                // Ensure no duplicate canonical skills (e.g., both "core java" and "java 17" map to "java")
                if (uniqueCanonicalNames.add(canonicalSkill.getName())) {
                    validSkills.add(ValidatedSkillDto.builder()
                            .skill(canonicalSkill)
                            .importanceScore(dto.getImportanceScore())
                            .build());
                }
            }
        }

        return validSkills;
    }

    /**
     * Normalizes the skill name: lowercase, trim, remove unexpected special chars (keep alphanumeric and spaces).
     */
    private String normalizeText(String input) {
        if (input == null) return "";
        return input.trim().toLowerCase().replaceAll("[^a-z0-9\\s+#\\.]", "");
    }

    /**
     * Look up the skill exactly by name first, if not found, check the alias table.
     */
    private Optional<Skill> findSkillInDatabase(String term) {
        // Direct Name Match
        Optional<Skill> explicitMatch = skillRepository.findByNameIgnoreCase(term);
        if (explicitMatch.isPresent()) {
            return explicitMatch;
        }

        // Alias Match
        Optional<SkillAlias> aliasMatch = skillAliasRepository.findByAliasNameIgnoreCase(term);
        return aliasMatch.map(SkillAlias::getSkill);
    }
}
