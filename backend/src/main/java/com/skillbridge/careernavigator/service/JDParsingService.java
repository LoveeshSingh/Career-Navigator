package com.skillbridge.careernavigator.service;

import com.skillbridge.careernavigator.dto.ParsedSkillDto;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.entity.SkillAlias;
import com.skillbridge.careernavigator.repository.SkillRepository;
import com.skillbridge.careernavigator.util.TextNormalizationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JDParsingService {

    private final SkillRepository skillRepository;

    /**
     * Parses the JD text deterministically by matching against the entire skill database.
     * Applies scoring based on context keywords (required, preferred) and frequency.
     */
    @Transactional(readOnly = true)
    public List<ParsedSkillDto> parseJobDescription(String jdText) {
        if (jdText == null || jdText.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Normalize and split JD into lines/sentences
        String[] chunks = jdText.split("\\n|\\.\\s+");
        List<String> normalizedLines = Arrays.stream(chunks)
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());

        // 2. Load all skills and their aliases from DB
        List<Skill> allSkills = skillRepository.findAll();
        
        // 3. Match and Score
        Map<UUID, Double> skillScores = new HashMap<>();
        Map<UUID, Skill> skillMap = new HashMap<>();

        for (Skill skill : allSkills) {
            skillMap.put(skill.getId(), skill);
            double score = 0;

            // Prepare match targets: Skill name + Aliases
            List<String> targets = new ArrayList<>();
            targets.add(TextNormalizationUtils.normalizeText(skill.getName()));
            for (SkillAlias alias : skill.getAliases()) {
                targets.add(TextNormalizationUtils.normalizeText(alias.getAliasName()));
            }

            for (String line : normalizedLines) {
                boolean matchedInLine = false;
                int lineOccurrenceCount = 0;

                for (String target : targets) {
                    String regex = TextNormalizationUtils.buildBoundaryRegex(target);
                    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                    var matcher = pattern.matcher(line);
                    
                    while (matcher.find()) {
                        matchedInLine = true;
                        lineOccurrenceCount++;
                    }
                }

                if (matchedInLine) {
                    // Context Bonus
                    if (containsKeyword(line, "required", "must", "mandatory", "essential")) {
                        score += 10;
                    } else if (containsKeyword(line, "preferred", "nice to have", "plus", "desirable")) {
                        score += 5;
                    }
                    
                    // Frequency Bonus
                    score += (lineOccurrenceCount * 2);
                }
            }

            if (score > 0) {
                // Add base score from DB as a minor factor (normalized to 0.1-1.0 range or just raw)
                // This acts as a tie-breaker for skills with same keyword/frequency importance
                double finalScore = score + (skill.getScore() / 1000.0);
                skillScores.put(skill.getId(), finalScore);
            }
        }

        // 4. Convert to DTOs
        return skillScores.entrySet().stream()
                .map(entry -> new ParsedSkillDto(skillMap.get(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());
    }

    private boolean containsKeyword(String line, String... keywords) {
        for (String kw : keywords) {
            if (line.contains(kw)) {
                return true;
            }
        }
        return false;
    }
}
