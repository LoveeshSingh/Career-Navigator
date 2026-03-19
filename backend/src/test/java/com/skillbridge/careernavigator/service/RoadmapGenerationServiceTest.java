package com.skillbridge.careernavigator.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.exception.RoadmapGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoadmapGenerationServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    private RoadmapGenerationService roadmapGenerationService;

    @Mock
    private RestTemplate restTemplate;

    private Skill javaSkill;

    @BeforeEach
    void setUp() {
        roadmapGenerationService = new RoadmapGenerationService(objectMapper);
        ReflectionTestUtils.setField(roadmapGenerationService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(roadmapGenerationService, "llmApiUrl", "http://fake-api");
        ReflectionTestUtils.setField(roadmapGenerationService, "llmApiKey", "fake-key");

        javaSkill = new Skill(); 
        javaSkill.setId(UUID.randomUUID()); 
        javaSkill.setName("Java");
    }

    @Test
    void generateRoadmap_ValidResponse() throws Exception {
        // Arrange
        Map<String, Object> textPart = Map.of("text", "{\"roadmap_details\": \"Step 1\", \"suggested_certifications\": [\"Cert A\"]}");
        Map<String, Object> content = Map.of("parts", Collections.singletonList(textPart));
        Map<String, Object> candidate = Map.of("content", content);
        Map<String, Object> mockResponse = Map.of("candidates", Collections.singletonList(candidate));

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        Map<String, Object> expectedOutput = new HashMap<>();
        expectedOutput.put("roadmap_details", "Step 1");
        expectedOutput.put("suggested_certifications", Collections.singletonList("Cert A"));

        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(expectedOutput);

        // Act
        Map<String, Object> roadmap = roadmapGenerationService.generateRoadmap(Collections.singletonList(javaSkill), "beginner", 10);

        // Assert
        assertThat(roadmap).containsKey("roadmap_details");
        assertThat(roadmap.get("roadmap_details")).isEqualTo("Step 1");
        assertThat((List)roadmap.get("suggested_certifications")).contains("Cert A");
    }

    @Test
    void generateRoadmap_EmptyResponse() {
        assertThrows(RoadmapGenerationException.class, () -> {
            Map<String, Object> mockResponse = Map.of("candidates", Collections.emptyList());
            ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

            when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                    .thenReturn(responseEntity);

            roadmapGenerationService.generateRoadmap(Collections.singletonList(javaSkill), "beginner", 10);
        });
    }

    @Test
    void generateRoadmap_ApiTimeout() {
        assertThrows(RoadmapGenerationException.class, () -> {
            when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                    .thenThrow(new RestClientException("Timeout"));

            roadmapGenerationService.generateRoadmap(Collections.singletonList(javaSkill), "beginner", 10);
        });
    }
}
