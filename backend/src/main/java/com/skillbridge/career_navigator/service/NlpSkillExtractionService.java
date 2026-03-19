package com.skillbridge.career_navigator.service;

import com.skillbridge.career_navigator.dto.ExtractedSkillDto;
import com.skillbridge.career_navigator.exception.NlpExtractionException;
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
public class NlpSkillExtractionService {

    private final RestTemplate restTemplate;
    private final String nlpApiUrl;
    private final String nlpApiKey;

    public NlpSkillExtractionService(
            @Value("${nlp.api.url}") String nlpApiUrl,
            @Value("${nlp.api.key}") String nlpApiKey) {
        
        // Initializing a local RestTemplate.
        // In a real application, this might be injected via @Bean to handle connection pooling and timeouts robustly.
        this.restTemplate = new RestTemplate();
        this.nlpApiUrl = nlpApiUrl;
        this.nlpApiKey = nlpApiKey;
    }

    /**
     * Sends the raw Job Description text to an external NLP API and extracts the ranked skills.
     * 
     * @param jobDescriptionText The raw text of the job description.
     * @return List of extracted skills ordered by importance/rank.
     */
    public List<ExtractedSkillDto> extractSkillsFromJobDescription(String jobDescriptionText) {
        if (jobDescriptionText == null || jobDescriptionText.trim().isEmpty()) {
            throw new NlpExtractionException("Job description text cannot be empty.");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + nlpApiKey);
            headers.set("Content-Type", "application/json");

            Map<String, String> body = new HashMap<>();
            body.put("text", jobDescriptionText);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

            // Mocking standard external NLP Response shape (e.g., {"skills": [{"name": "Java", "score": 95.5}]})
            // Using a generic Map structure for parsing given the hypothetical API format.
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    nlpApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            return parseApiResponse(responseEntity.getBody());

        } catch (RestClientException ex) {
            // Catches timeouts, 4xx, 5xx, or connection refused errors.
            throw new NlpExtractionException("Failed to communicate with the NLP Extraction API: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            // General safety net for unexpected parsing crashes.
            throw new NlpExtractionException("An unexpected error occurred during NLP extraction.", ex);
        }
    }

    /**
     * Parses the external API response payload and normalizes the skill outputs.
     */
    @SuppressWarnings("unchecked")
    private List<ExtractedSkillDto> parseApiResponse(Map<String, Object> responseBody) {
        if (responseBody == null || !responseBody.containsKey("skills")) {
            throw new NlpExtractionException("Invalid response from NLP API: Missing 'skills' payload.");
        }

        List<Map<String, Object>> skillsArray = (List<Map<String, Object>>) responseBody.get("skills");

        if (skillsArray == null || skillsArray.isEmpty()) {
            return Collections.emptyList();
        }

        return skillsArray.stream()
                .map(skillData -> {
                    String rawName = (String) skillData.getOrDefault("name", "");
                    Double score = parseScore(skillData.get("score"));

                    // Normalize: lowercase and trim
                    String normalizedName = rawName.trim().toLowerCase();

                    return ExtractedSkillDto.builder()
                            .skillName(normalizedName)
                            .importanceScore(score)
                            .build();
                })
                .filter(dto -> !dto.getSkillName().isEmpty())
                // Sort by highest score first
                .sorted((s1, s2) -> Double.compare(s2.getImportanceScore(), s1.getImportanceScore()))
                .collect(Collectors.toList());
    }

    private Double parseScore(Object scoreObj) {
        if (scoreObj instanceof Number) {
            return ((Number) scoreObj).doubleValue();
        } else if (scoreObj instanceof String) {
            try {
                return Double.parseDouble((String) scoreObj);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}
