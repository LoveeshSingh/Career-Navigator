package com.skillbridge.careernavigator.service;

import com.skillbridge.careernavigator.dto.FallbackDataDto;
import com.skillbridge.careernavigator.dto.FallbackResponseDto;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FallbackService {

    private final SkillRepository skillRepository;

    /**
     * Executes the absolute fallback circuit. Called explicitly only when LLM pipelines time out or hallucinate.
     * Guaranteed to return a valid URL for every registered canonical skill by forcing "BEGINNER" as a base trapdoor.
     */
    @Transactional(readOnly = true)
    public FallbackResponseDto generateFallbackRoadmap(List<Skill> missingSkills, String level) {
        log.warn("FALLBACK CIRCUIT BREAKER TRIPPED! Routing {} missing skills to dynamic YouTube searches at level: {}", 
                 missingSkills != null ? missingSkills.size() : 0, level);

        if (missingSkills == null || missingSkills.isEmpty()) {
            return new FallbackResponseDto("fallback", new ArrayList<>());
        }

        List<FallbackDataDto> dataList = new ArrayList<>();
        String normalizedTargetLevel = normalizeLevel(level);

        for (Skill skill : missingSkills) {
            String dynamicSearchUrl = generateDynamicSearchUrl(skill.getName(), normalizedTargetLevel);
            
            FallbackDataDto dataDto = FallbackDataDto.builder()
                    .skill(skill.getName())
                    .video(dynamicSearchUrl)
                    .build();
                    
            dataList.add(dataDto);
        }

        return FallbackResponseDto.builder()
                .mode("fallback")
                .data(dataList)
                .build();
    }

    /**
     * Constructs a dynamic YouTube search URL based on skill name and targeted level.
     * This replaces the need for static database entries for every skill/level pair.
     */
    private String generateDynamicSearchUrl(String skillName, String level) {
        String query = skillName.replace(" ", "+") + "+for+" + level;
        return "https://www.youtube.com/results?search_query=" + query;
    }

    private String normalizeLevel(String level) {
        return (level == null || level.trim().isEmpty()) ? "beginner" : level.trim().toLowerCase();
    }
}
