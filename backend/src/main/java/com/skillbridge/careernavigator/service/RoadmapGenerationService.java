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
        // Ensure UTF-8 for API response
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(0, new org.springframework.http.converter.StringHttpMessageConverter(java.nio.charset.StandardCharsets.UTF_8));
        this.objectMapper = objectMapper;
        // Configure Jackson to be more lenient with AI output
        this.objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        this.objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
    }

    /**
     * Sends missing skills to Google Gemini using the most stable REST API format.
     * Uses robust regex-based JSON extraction to handle various response formats.
     */
    public Map<String, Object> generateRoadmap(List<Skill> missingSkills, String level, int hoursPerWeek, String role) {
        if (missingSkills == null || missingSkills.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> skillNames = missingSkills.stream()
                .map(Skill::getName)
                .collect(Collectors.toList());

        String prompt = buildPrompt(skillNames, level, hoursPerWeek, role);

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
            // Log the request payload for debugging
            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.debug("Gemini Request Payload: {}", requestJson);

            ResponseEntity<String> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    String.class);

            return parseAndValidateGeminiResponse(response.getBody());

        } catch (Exception e) {
            log.error("Gemini Execution or Parse Failure. Check if API key is valid and quota is available.", e);
            throw new RoadmapGenerationException("Failed to generate AI roadmap", e);
        }
    }

    private String buildPrompt(List<String> skills, String level, int hoursPerWeek, String role) {
        String skillsList = String.join(", ", skills);
        String roleSegment = (role != null && !role.isEmpty()) ? " as a '" + role + "'" : "";
        String prompt = String.format(
                "You are an expert career consultant. Generate a 5-WEEK learning roadmap for these skills%s: %s. " +
                "User level: %s, Time: %d hrs/week. " +
                "Return ONLY a JSON object: { \"roadmap_details\": \"Markdown string\", \"suggested_certifications\": [\"String\"] }",
                roleSegment, skillsList, level, hoursPerWeek);
        return prompt;
    }

    private Map<String, Object> parseAndValidateGeminiResponse(String responseJson) throws Exception {
        if (responseJson == null || responseJson.isEmpty()) {
            throw new RoadmapGenerationException("Gemini returned an empty response body.");
        }

        log.debug("Full API Response: {}", responseJson);

        Map<String, Object> responseBody;
        try {
            responseBody = objectMapper.readValue(responseJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Failed to parse top-level API response.");
            throw e;
        }

        if (responseBody.containsKey("error")) {
            Map<String, Object> error = (Map<String, Object>) responseBody.get("error");
            throw new RoadmapGenerationException("Gemini API Error: " + error.get("message"));
        }

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new RoadmapGenerationException("Gemini returned no candidates.");
        }

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        String text = (String) parts.get(0).get("text");

        // Extract JSON block
        String jsonText = text.trim();
        int start = jsonText.indexOf('{');
        int end = jsonText.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            jsonText = jsonText.substring(start, end + 1);
        }

        // Clean up common AI formatting artifacts
        jsonText = jsonText.replaceAll("[\\p{Cc}&&[^\\r\\n\\t]]", ""); // Control chars

        try {
            Map<String, Object> result = objectMapper.readValue(jsonText, new TypeReference<Map<String, Object>>() {});
            
            // Critical Fix: Unescape literal "\n" strings that AI sometimes returns instead of real newlines
            if (result.containsKey("roadmap_details") && result.get("roadmap_details") instanceof String) {
                String details = (String) result.get("roadmap_details");
                details = details.replace("\\n", "\n").replace("\\\"", "\"");
                result.put("roadmap_details", details);
            }
            
            return result;
        } catch (Exception e) {
            log.error("CRITICAL: JSON Parse Failure. Full extracted text for debugging: \n[[---START---]]\n{}\n[[---END---]]", jsonText);
            
            int errorOffset = -1;
            if (e instanceof com.fasterxml.jackson.core.JsonParseException) {
                errorOffset = (int) ((com.fasterxml.jackson.core.JsonParseException) e).getLocation().getCharOffset();
            }
            
            if (errorOffset != -1 && errorOffset < jsonText.length()) {
                int startSnippet = Math.max(0, errorOffset - 100);
                int endSnippet = Math.min(jsonText.length(), errorOffset + 100);
                log.error("JSON Error Detail: Offset {}. Snippet: ...{} >>>|{}|<<< {}...", 
                    errorOffset, 
                    jsonText.substring(startSnippet, errorOffset),
                    jsonText.charAt(errorOffset),
                    jsonText.substring(errorOffset + 1, endSnippet));
            }
            throw e;
        }
    }
}
