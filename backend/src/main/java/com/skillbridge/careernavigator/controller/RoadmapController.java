package com.skillbridge.careernavigator.controller;

import com.skillbridge.careernavigator.dto.FallbackResponseDto;
import com.skillbridge.careernavigator.dto.ParsedSkillDto;
import com.skillbridge.careernavigator.dto.ResumeMatchResultDto;
import com.skillbridge.careernavigator.dto.RoadmapRequestDto;
import com.skillbridge.careernavigator.dto.RoadmapResponseDto;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.service.FallbackService;
import com.skillbridge.careernavigator.service.JDParsingService;
import com.skillbridge.careernavigator.service.ResumeMatchingService;
import com.skillbridge.careernavigator.service.RoadmapGenerationService;
import com.skillbridge.careernavigator.service.SkillSelectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/roadmap")
@RequiredArgsConstructor
@CrossOrigin("*")
@Validated
public class RoadmapController {

    private final JDParsingService jdParsingService;
    private final SkillSelectionService skillSelectionService;
    private final ResumeMatchingService resumeMatchingService;
    private final RoadmapGenerationService roadmapGenerationService;
    private final FallbackService fallbackService;

    @PostMapping("/generate")
    public ResponseEntity<RoadmapResponseDto> generateRoadmap(@Valid @RequestBody RoadmapRequestDto request) {
        log.info("Incoming Roadmap Request: jdPresent={}, roleId={}, topK={}", 
            request.getJdText() != null && !request.getJdText().trim().isEmpty(),
            request.getRoleId(), request.getTopK());
        
        // 1. Validation constraints
        if (request.getResumeText() == null || request.getResumeText().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(RoadmapResponseDto.builder()
                    .mode("error")
                    .message("Resume text is strictly required.")
                    .build());
        }
        
        boolean hasJd = request.getJdText() != null && !request.getJdText().trim().isEmpty();
        boolean hasRole = request.getRoleId() != null;
        
        if (!hasJd && !hasRole) {
            return ResponseEntity.badRequest().body(RoadmapResponseDto.builder()
                    .mode("error")
                    .message("Either roleId or jdText must be provided.")
                    .build());
        }

        int topK = (request.getTopK() != null && request.getTopK() > 0) ? Math.min(request.getTopK(), 20) : 10;
        int hoursPerWeek = (request.getHoursPerWeek() != null && request.getHoursPerWeek() > 0) ? request.getHoursPerWeek() : 10;
        String level = (request.getLevel() != null && !request.getLevel().trim().isEmpty()) ? request.getLevel() : "beginner";

        List<Skill> selectedSkills;

        // 2. Branch Mapping
        if (hasJd) {
            // New Deterministic JD Flow
            List<ParsedSkillDto> parsedSkills = jdParsingService.parseJobDescription(request.getJdText());
            selectedSkills = skillSelectionService.selectSkillsForJd(parsedSkills, topK);
        } else {
            // Role Flow
            selectedSkills = skillSelectionService.selectSkillsForRole(request.getRoleId(), topK);
        }

        // 3. Resume Cross-referencing
        ResumeMatchResultDto matchResult = resumeMatchingService.matchSkills(request.getResumeText(), selectedSkills);
        List<Skill> missingSkills = matchResult.getMissingSkills();
        List<Skill> presentSkills = matchResult.getPresentSkills();

        // 4. Transform for DTO
        List<String> missingSkillNames = missingSkills.stream().map(Skill::getName).collect(Collectors.toList());
        List<String> presentSkillNames = presentSkills.stream().map(Skill::getName).collect(Collectors.toList());
        int totalSkills = missingSkills.size() + presentSkills.size();
        double matchPercentage = totalSkills > 0 ? (presentSkills.size() * 100.0 / totalSkills) : 100.0;

        // 5. Fulfillment Check
        if (missingSkills.isEmpty()) {
            return ResponseEntity.ok(RoadmapResponseDto.builder()
                    .mode("ai")
                    .message("You are already aligned with required skills")
                    .matchPercentage(100.0)
                    .presentSkills(presentSkillNames)
                    .build());
        }

        // 6. Generative LLM Execution
        try {
            Map<String, Object> llmRoadmap = roadmapGenerationService.generateRoadmap(missingSkills, level, hoursPerWeek, request.getRole());
            
            return ResponseEntity.ok(RoadmapResponseDto.builder()
                    .mode("ai")
                    .data(llmRoadmap)
                    .missingSkills(missingSkillNames)
                    .presentSkills(presentSkillNames)
                    .matchPercentage(matchPercentage)
                    .build());
                    
        } catch (RuntimeException e) {
            // 7. Absolute Fallback Route
            FallbackResponseDto fallbackPayload = fallbackService.generateFallbackRoadmap(missingSkills, level);
            
            return ResponseEntity.ok(RoadmapResponseDto.builder()
                    .mode("fallback")
                    .message("AI generation failed. Degrading to predefined standard video paths.")
                    .data(fallbackPayload.getData())
                    .missingSkills(missingSkillNames)
                    .presentSkills(presentSkillNames)
                    .matchPercentage(matchPercentage)
                    .build());
        }
    }
}
