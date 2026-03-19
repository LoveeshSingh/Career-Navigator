package com.skillbridge.career_navigator.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.career_navigator.entity.Skill;
import com.skillbridge.career_navigator.exception.RoadmapGenerationException;
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
     * Sends missing skills to an LLM to generate a strictly formatted JSON roadmap.
     * Prevents hallucination by enforcing a strict constraint against adding missing skills.
     */
    public Map<String, List<String>> generateRoadmap(List<Skill> missingSkills, String level, int hoursPerWeek) {
        if (missingSkills == null || missingSkills.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> skillNames = missingSkills.stream()
                .map(Skill::getName)
                .collect(Collectors.toList());

        String prompt = buildPrompt(skillNames, level, hoursPerWeek);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); // Or configurable model
        requestBody.put("messages", Arrays.asList(
                Map.of("role", "system", "content", "You are a strict JSON-only API. You output raw JSON objects. Do not include markdown blocks like ```json."),
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.1); 

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + llmApiKey);
        headers.set("Content-Type", "application/json");

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    llmApiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Map.class
            );

            return parseAndValidateResponse(response.getBody(), skillNames);

        } catch (RestClientException e) {
            throw new RoadmapGenerationException("LLM API network failure or timeout", e);
        } catch (Exception e) {
            throw new RoadmapGenerationException("Failed to parse LLM roadmap generation", e);
        }
    }

    private String buildPrompt(List<String> skills, String level, int hoursPerWeek) {
        String skillsList = String.join(", ", skills);
        return String.format(
                "Create a learning roadmap for a %s level student planning to study %d hours per week. " +
                "You MUST distribute the following exact skills into a weekly plan: [%s].\n\n" +
                "CRITICAL CONSTRAINTS:\n" +
                "1. DO NOT add any new skills not listed above.\n" +
                "2. DO NOT omit any of the listed skills.\n" +
                "3. Output MUST be a valid raw JSON object mapping string keys (like 'week_1') to an array of strings (the skills).\n\n" +
                "Example Format:\n" +
                "{\n" +
                "  \"week_1\": [\"java\", \"spring boot\"],\n" +
                "  \"week_2\": [\"git\"]\n" +
                "}",
                level, hoursPerWeek, skillsList
        );
    }

    private Map<String, List<String>> parseAndValidateResponse(Map<String, Object> responseBody, List<String> originalSkills) throws Exception {
        if (responseBody == null || !responseBody.containsKey("choices")) {
            throw new RoadmapGenerationException("Invalid LLM response format");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (choices.isEmpty()) {
            throw new RoadmapGenerationException("Empty choices from LLM");
        }

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");
        
        // Clean markdown backticks if LLM ignores system prompt
        if (content.startsWith("```json")) {
            content = content.replace("```json", "").replace("```", "").trim();
        } else if (content.startsWith("```")) {
            content = content.replace("```", "").trim();
        }

        Map<String, List<String>> roadmap = objectMapper.readValue(content, new TypeReference<Map<String, List<String>>>() {});
        
        if (roadmap == null || roadmap.isEmpty()) {
            throw new RoadmapGenerationException("Parsed roadmap is empty");
        }

        // Validate that no hallucinated skills were added
        Set<String> validSet = new HashSet<>(originalSkills);
        for (List<String> weekSkills : roadmap.values()) {
            for (String skill : weekSkills) {
                if (!validSet.contains(skill)) {
                    throw new RoadmapGenerationException("LLM hallucinated a new skill: " + skill);
                }
            }
        }

        return roadmap;
    }
}
