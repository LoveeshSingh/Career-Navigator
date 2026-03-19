package com.skillbridge.careernavigator.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.exception.RoadmapGenerationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RoadmapGenerationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${llm.api.url}")
    private String llmApiUrl;

    @Value("${llm.api.key}")
    private String llmApiKey;

    public RoadmapGenerationService(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    /**
     * Sends missing skills to Google Gemini using the most stable REST API format.
     * Uses robust regex-based JSON extraction to handle various response formats.
     */
    public Map<String, Object> generateRoadmap(List<Skill> missingSkills, String level, int hoursPerWeek) {
        if (missingSkills == null || missingSkills.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> skillNames = missingSkills.stream()
                .map(Skill::getName)
                .collect(Collectors.toList());

        String prompt = buildPrompt(skillNames, level, hoursPerWeek);

        // Simple Schema: { "contents": [{ "parts": [{ "text": "..." }] }] }
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        
        Map<String, Object> contentPart = new HashMap<>();
        contentPart.put("parts", Collections.singletonList(textPart));
        requestBody.put("contents", Collections.singletonList(contentPart));

        // API Key is passed via Query Parameter
        String fullUrl = llmApiUrl + "?key=" + llmApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        log.info("Dispatching Refined Gemini Request for {} skills at level {} with {} hrs/week", 
                missingSkills.size(), level, hoursPerWeek);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Map.class
            );

            return parseAndValidateGeminiResponse(response.getBody());

        } catch (Exception e) {
            log.error("Gemini Execution or Parse Failure.", e);
            throw new RoadmapGenerationException("Failed to generate AI roadmap", e);
        }
    }

    private String buildPrompt(List<String> skills, String level, int hoursPerWeek) {
        String skillsList = String.join(", ", skills);
        return String.format(
                "You are an expert career and technical consultant. Generate a highly detailed learning roadmap for these skills: %s. " +
                "The user is currently at a '%s' level and can commit %d hours per week. " +
                "Provide your response STRICTLY as a JSON object with exactly these two keys:\n" +
                "1. 'roadmap_details': A single detailed string (formatted with Markdown) that explains exactly what topics to study week-by-week (e.g., Week 1, Week 2, etc.) based on the user's available time. " +
                "Do not use separate JSON keys for weeks; use one formatted string.\n" +
                "2. 'suggested_certifications': A JSON list of professional certifications (from reputable providers like IBM, Google, AWS, Microsoft, Oracle, etc.) that would validate these specific skills.\n\n" +
                "Format the 'roadmap_details' string with clear headers, bullet points, and realistic learning milestones. " +
                "Return ONLY the JSON. No markdown code blocks, no preamble, no tailing text.",
                skillsList, level, hoursPerWeek
        );
    }

    private Map<String, Object> parseAndValidateGeminiResponse(Map<String, Object> responseBody) throws Exception {
        if (responseBody == null || !responseBody.containsKey("candidates")) {
            throw new RoadmapGenerationException("Invalid Gemini response format");
        }

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        String text = (String) parts.get(0).get("text");

        log.debug("Raw Gemini Response: {}", text);

        // Robust JSON Extraction (handles markdown ```json ... ```)
        String jsonText = text;
        if (text.contains("{")) {
            jsonText = text.substring(text.indexOf("{"), text.lastIndexOf("}") + 1);
        }

        return objectMapper.readValue(jsonText, new TypeReference<Map<String, Object>>() {});
    }
}
