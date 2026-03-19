package com.skillbridge.careernavigator.service;

import com.skillbridge.careernavigator.dto.FallbackResponseDto;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FallbackServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private FallbackService fallbackService;

    private Skill javaSkill;

    @BeforeEach
    void setUp() {
        javaSkill = new Skill();
        javaSkill.setId(UUID.randomUUID());
        javaSkill.setName("Java");
    }

    @Test
    void generateFallbackRoadmap_GeneratesDynamicSearchUrl() {
        // Act
        FallbackResponseDto result = fallbackService.generateFallbackRoadmap(List.of(javaSkill), "intermediate");

        // Assert
        assertThat(result.getMode()).isEqualTo("fallback");
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getSkill()).isEqualTo("Java");
        assertThat(result.getData().get(0).getVideo()).isEqualTo("https://www.youtube.com/results?search_query=Java+for+intermediate");
    }

    @Test
    void generateFallbackRoadmap_HandlesSpacesInSkillName() {
        Skill springSkill = new Skill();
        springSkill.setName("Spring Boot");
        
        // Act
        FallbackResponseDto result = fallbackService.generateFallbackRoadmap(List.of(springSkill), "beginner");

        // Assert
        assertThat(result.getData().get(0).getVideo()).isEqualTo("https://www.youtube.com/results?search_query=Spring+Boot+for+beginner");
    }

    @Test
    void generateFallbackRoadmap_EmptySkillsList() {
        FallbackResponseDto result = fallbackService.generateFallbackRoadmap(List.of(), "beginner");
        assertThat(result.getData()).isEmpty();
    }
}
