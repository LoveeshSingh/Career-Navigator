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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/roadmap")
@RequiredArgsConstructor
@Validated
public class RoadmapController {

    private final JDParsingService jdParsingService;
    private final SkillSelectionService skillSelectionService;
    private final ResumeMatchingService resumeMatchingService;
    private final RoadmapGenerationService roadmapGenerationService;
    private final FallbackService fallbackService;

    @PostMapping("/generate")
    public ResponseEntity<RoadmapResponseDto> generateRoadmap(@Valid @RequestBody RoadmapRequestDto request) {
        log.info("Incoming Roadmap Generation Request. Has JD: {}, Has Role: {}, TopK: {}" , 
            request.getJdText() != null, request.getRoleId() != null, request.getTopK());
        
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
                    
        } catch (RuntimeException e) {
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
