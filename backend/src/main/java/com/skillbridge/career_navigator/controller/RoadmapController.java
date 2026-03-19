package com.skillbridge.career_navigator.controller;

import com.skillbridge.career_navigator.dto.*;
import com.skillbridge.career_navigator.entity.Skill;
import com.skillbridge.career_navigator.exception.RoadmapGenerationException;
import com.skillbridge.career_navigator.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/roadmap")
@RequiredArgsConstructor
public class RoadmapController {

    private final NlpSkillExtractionService nlpSkillExtractionService;
    private final SkillValidationService skillValidationService;
    private final SkillSelectionService skillSelectionService;
    private final ResumeMatchingService resumeMatchingService;
    private final RoadmapGenerationService roadmapGenerationService;
    private final FallbackService fallbackService;

    @PostMapping("/generate")
    public ResponseEntity<RoadmapResponseDto> generateRoadmap(@RequestBody RoadmapRequestDto request) {
        
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
            // JD Flow
            List<ExtractedSkillDto> rawSkills = nlpSkillExtractionService.extractSkillsFromJobDescription(request.getJdText());
            List<ValidatedSkillDto> validSkills = skillValidationService.validateSkills(rawSkills);
            selectedSkills = skillSelectionService.selectSkillsForJd(validSkills, topK);
        } else {
            // Role Flow
            selectedSkills = skillSelectionService.selectSkillsForRole(request.getRoleId(), topK);
        }

        // 3. Resume Cross-referencing
        ResumeMatchResultDto matchResult = resumeMatchingService.matchSkills(request.getResumeText(), selectedSkills);
        List<Skill> missingSkills = matchResult.getMissingSkills();

        // 4. Fulfillment Check
        if (missingSkills.isEmpty()) {
            return ResponseEntity.ok(RoadmapResponseDto.builder()
                    .mode("ai")
                    .message("You are already aligned with required skills")
                    .build());
        }

        // 5. Generative LLM Execution bounded by catch blocks
        try {
            Map<String, List<String>> llmRoadmap = roadmapGenerationService.generateRoadmap(missingSkills, level, hoursPerWeek);
            
            return ResponseEntity.ok(RoadmapResponseDto.builder()
                    .mode("ai")
                    .data(llmRoadmap)
                    .build());
                    
        } catch (RoadmapGenerationException | RuntimeException e) {
            // 6. Absolute Fallback Route
            FallbackResponseDto fallbackPayload = fallbackService.generateFallbackRoadmap(missingSkills, level);
            
            return ResponseEntity.ok(RoadmapResponseDto.builder()
                    .mode("fallback")
                    .message("AI generation failed. Degrading to predefined standard video paths.")
                    .data(fallbackPayload.getData())
                    .build());
        }
    }
}
