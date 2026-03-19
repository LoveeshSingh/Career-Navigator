package com.skillbridge.careernavigator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.careernavigator.dto.*;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.exception.RoadmapGenerationException;
import com.skillbridge.careernavigator.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoadmapController.class)
class RoadmapControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NlpSkillExtractionService nlpSkillExtractionService;

    @MockitoBean
    private SkillValidationService skillValidationService;

    @MockitoBean
    private SkillSelectionService skillSelectionService;

    @MockitoBean
    private ResumeMatchingService resumeMatchingService;

    @MockitoBean
    private RoadmapGenerationService roadmapGenerationService;

    @MockitoBean
    private FallbackService fallbackService;

    @Test
    void generateRoadmap_JdFlow_LLMSuccess() throws Exception {
        RoadmapRequestDto request = RoadmapRequestDto.builder()
                .jdText("We need Java and Spring")
                .resumeText("I know HTML")
                .level("beginner")
                .hoursPerWeek(10)
                .topK(10)
                .build();

        Skill mockSkill = new Skill(); mockSkill.setName("Java");

        when(nlpSkillExtractionService.extractSkillsFromJobDescription(anyString()))
                .thenReturn(List.of(new ExtractedSkillDto("Java", 9.0)));
        when(skillValidationService.validateSkills(anyList()))
                .thenReturn(List.of(new ValidatedSkillDto(mockSkill, 9.0)));
        when(skillSelectionService.selectSkillsForJd(anyList(), anyInt()))
                .thenReturn(List.of(mockSkill));
        when(resumeMatchingService.matchSkills(anyString(), anyList()))
                .thenReturn(new ResumeMatchResultDto(List.of(), List.of(mockSkill))); // Java is missing

        Map<String, List<String>> mockRoadmap = Map.of("week_1", List.of("Java"));
        when(roadmapGenerationService.generateRoadmap(anyList(), anyString(), anyInt()))
                .thenReturn(mockRoadmap);

        mockMvc.perform(post("/api/v1/roadmap/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("ai"))
                .andExpect(jsonPath("$.data.week_1[0]").value("Java"));
    }

    @Test
    void generateRoadmap_JdFlow_LLMFailure_TriggersFallback() throws Exception {
        RoadmapRequestDto request = RoadmapRequestDto.builder()
                .jdText("We need Java")
                .resumeText("I know HTML")
                .level("beginner")
                .hoursPerWeek(10)
                .topK(10)
                .build();

        Skill mockSkill = new Skill(); mockSkill.setName("Java");

        when(nlpSkillExtractionService.extractSkillsFromJobDescription(anyString()))
                .thenReturn(List.of());
        when(skillValidationService.validateSkills(anyList())).thenReturn(List.of());
        when(skillSelectionService.selectSkillsForJd(anyList(), anyInt())).thenReturn(List.of(mockSkill));
        when(resumeMatchingService.matchSkills(anyString(), anyList()))
                .thenReturn(new ResumeMatchResultDto(List.of(), List.of(mockSkill)));

        // Simulate LLM Network Crash
        when(roadmapGenerationService.generateRoadmap(anyList(), anyString(), anyInt()))
                .thenThrow(new RoadmapGenerationException("Timeout"));

        // Simulate Fallback Triggering
        FallbackResponseDto fallbackResponse = FallbackResponseDto.builder()
                .mode("fallback")
                .data(List.of(FallbackDataDto.builder().skill("Java").video("http://youtube.com/java").build()))
                .build();
                
        when(fallbackService.generateFallbackRoadmap(anyList(), anyString()))
                .thenReturn(fallbackResponse);

        mockMvc.perform(post("/api/v1/roadmap/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("fallback"))
                .andExpect(jsonPath("$.data[0].skill").value("Java"))
                .andExpect(jsonPath("$.data[0].video").value("http://youtube.com/java"));
    }

    @Test
    void generateRoadmap_NoMissingSkills() throws Exception {
        RoadmapRequestDto request = RoadmapRequestDto.builder()
                .roleId(UUID.randomUUID())
                .resumeText("I am a Senior Java Developer")
                .level("intermediate")
                .hoursPerWeek(10)
                .topK(5)
                .build();

        Skill mockSkill = new Skill(); mockSkill.setName("Java");

        when(skillSelectionService.selectSkillsForRole(any(UUID.class), anyInt()))
                .thenReturn(List.of(mockSkill));
        when(resumeMatchingService.matchSkills(anyString(), anyList()))
                .thenReturn(new ResumeMatchResultDto(List.of(mockSkill), List.of())); // No missing skills!

        mockMvc.perform(post("/api/v1/roadmap/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("ai"))
                .andExpect(jsonPath("$.message").value("You are already aligned with required skills"));
    }

    @Test
    void generateRoadmap_ValidationFailure_MissingResume() throws Exception {
        RoadmapRequestDto request = RoadmapRequestDto.builder()
                .roleId(UUID.randomUUID())
                .resumeText("") // Intentionally blank
                .level("beginner")
                .build();

        mockMvc.perform(post("/api/v1/roadmap/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // 400 Bad Request mapped from Jakarta @Valid
    }
}
