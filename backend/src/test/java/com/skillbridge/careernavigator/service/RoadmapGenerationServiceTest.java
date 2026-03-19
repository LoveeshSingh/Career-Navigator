package com.skillbridge.careernavigator.service;

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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    // Use a direct instance instead of a mock so we can inject RestTemplate via Reflection
    private RoadmapGenerationService roadmapGenerationService;

    @Mock
    private RestTemplate restTemplate;

    private Skill javaSkill, sqlSkill;

    @BeforeEach
    void setUp() {
        roadmapGenerationService = new RoadmapGenerationService(objectMapper);
        ReflectionTestUtils.setField(roadmapGenerationService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(roadmapGenerationService, "llmApiUrl", "http://fake-api");
        ReflectionTestUtils.setField(roadmapGenerationService, "llmApiKey", "fake-key");

        javaSkill = new Skill(); javaSkill.setId(UUID.randomUUID()); javaSkill.setName("Java");
        sqlSkill = new Skill(); sqlSkill.setId(UUID.randomUUID()); sqlSkill.setName("SQL");
    }

    @Test
    void generateRoadmap_ValidResponse() throws Exception {
        // Arrange
        Map<String, Object> fakeMessage = Map.of("content", "{\"week_1\": [\"Java\"]}");
        Map<String, Object> fakeChoice = Map.of("message", fakeMessage);
        Map<String, Object> mockResponse = Map.of("choices", List.of(fakeChoice));

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        Map<String, List<String>> expectedMappedOutput = Map.of("week_1", List.of("Java"));
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(expectedMappedOutput);

        // Act
        Map<String, List<String>> roadmap = roadmapGenerationService.generateRoadmap(Arrays.asList(javaSkill), "beginner", 10);

        // Assert
        assertThat(roadmap).containsEntry("week_1", List.of("Java"));
    }

    @Test
    void generateRoadmap_EmptyResponse() {
        assertThrows(RoadmapGenerationException.class, () -> {
            Map<String, Object> mockResponse = Map.of("choices", List.of()); // Empty choices
            ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

            when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                    .thenReturn(responseEntity);

            roadmapGenerationService.generateRoadmap(Arrays.asList(javaSkill), "beginner", 10);
        });
    }

    @Test
    void generateRoadmap_ApiTimeout() {
        assertThrows(RoadmapGenerationException.class, () -> {
            when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                    .thenThrow(new RestClientException("Timeout"));

            roadmapGenerationService.generateRoadmap(Arrays.asList(javaSkill), "beginner", 10);
        });
    }

    @Test
    void generateRoadmap_HallucinatedSkillThrowsException() throws Exception {
        Map<String, Object> fakeMessage = Map.of("content", "{\"week_1\": [\"Java\", \"C++\"]}");
        Map<String, Object> fakeChoice = Map.of("message", fakeMessage);
        Map<String, Object> mockResponse = Map.of("choices", List.of(fakeChoice));

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        Map<String, List<String>> hallucinatedOutput = Map.of("week_1", Arrays.asList("Java", "C++")); // C++ wasn't sent
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(hallucinatedOutput);

        assertThrows(RoadmapGenerationException.class, () -> {
            roadmapGenerationService.generateRoadmap(Arrays.asList(javaSkill), "beginner", 10);
        });
    }
}
