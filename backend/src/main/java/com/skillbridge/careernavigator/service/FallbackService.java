package com.skillbridge.careernavigator.service;

import com.skillbridge.careernavigator.dto.FallbackDataDto;
import com.skillbridge.careernavigator.dto.FallbackResponseDto;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.entity.SkillContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FallbackService {

    /**
     * Executes the absolute fallback circuit. Called explicitly only when LLM pipelines time out or hallucinate.
     * Guaranteed to return a valid URL for every registered canonical skill by forcing "BEGINNER" as a base trapdoor.
     */
    @Transactional(readOnly = true)
    public FallbackResponseDto generateFallbackRoadmap(List<Skill> missingSkills, String level) {
        log.warn("FALLBACK CIRCUIT BREAKER TRIPPED! Routing {} missing skills to predefined SQL mappings at level: {}", 
                 missingSkills != null ? missingSkills.size() : 0, level);

        if (missingSkills == null || missingSkills.isEmpty()) {
            return new FallbackResponseDto("fallback", new ArrayList<>());
        }

        List<FallbackDataDto> dataList = new ArrayList<>();
        String normalizedTargetLevel = normalizeLevel(level);

        for (Skill skill : missingSkills) {
            String bestVideoUrl = findBestVideoMatch(skill, normalizedTargetLevel);
            
            FallbackDataDto dataDto = FallbackDataDto.builder()
                    .skill(skill.getName())
                    .video(bestVideoUrl)
                    .build();
                    
            dataList.add(dataDto);
        }

        return FallbackResponseDto.builder()
                .mode("fallback")
                .data(dataList)
                .build();
    }

    /**
     * Safely locates the tightest targeted video content bound to the required level.
     * Falls back to "BEGINNER" automatically if the requested string is completely empty or undefined on the DB entity.
     */
    private String findBestVideoMatch(Skill skill, String targetLevel) {
        List<SkillContent> contents = skill.getContents();
        if (contents == null || contents.isEmpty()) {
            // Absolute native fallback if database admin omitted video insertion during seeding
            return "https://youtube.com/results?search_query=" + skill.getName().replace(" ", "+") + "+tutorial";
        }

        // 1. Attempt exact match against requested level
        Optional<SkillContent> exactMatch = contents.stream()
                .filter(c -> normalizeLevel(c.getLevel()).equals(targetLevel))
                .findFirst();
                
        if (exactMatch.isPresent()) {
            return exactMatch.get().getUrl();
        }

        // 2. Cascade down to "BEGINNER" explicitly
        Optional<SkillContent> basicFallback = contents.stream()
                .filter(c -> normalizeLevel(c.getLevel()).equals("beginner"))
                .findFirst();
                
        if (basicFallback.isPresent()) {
            return basicFallback.get().getUrl();
        }

        // 3. Complete system trapdoor: Give them whatever exists first.
        return contents.get(0).getUrl();
    }

    private String normalizeLevel(String level) {
        return (level == null || level.trim().isEmpty()) ? "beginner" : level.trim().toLowerCase();
    }
}
