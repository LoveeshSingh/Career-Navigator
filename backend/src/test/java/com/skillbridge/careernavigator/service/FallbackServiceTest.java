package com.skillbridge.careernavigator.service;

import com.skillbridge.careernavigator.dto.FallbackResponseDto;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.entity.SkillContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FallbackServiceTest {

    private FallbackService fallbackService;

    private Skill javaSkill;
    private SkillContent javaBeginner;
    private SkillContent javaIntermediate;

    @BeforeEach
    void setUp() {
        fallbackService = new FallbackService();

        javaSkill = new Skill();
        javaSkill.setId(UUID.randomUUID());
        javaSkill.setName("Java");

        javaBeginner = new SkillContent();
        javaBeginner.setLevel("beginner");
        javaBeginner.setUrl("http://java-beginner");

        javaIntermediate = new SkillContent();
        javaIntermediate.setLevel("intermediate");
        javaIntermediate.setUrl("http://java-intermediate");

        javaSkill.setContents(Arrays.asList(javaBeginner, javaIntermediate));
    }

    @Test
    void generateFallbackRoadmap_ExactLevelMatch() {
        FallbackResponseDto result = fallbackService.generateFallbackRoadmap(List.of(javaSkill), "intermediate");

        assertThat(result.getMode()).isEqualTo("fallback");
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getVideo()).isEqualTo("http://java-intermediate");
    }

    @Test
    void generateFallbackRoadmap_MissingLevelFallsBackToBeginner() {
        FallbackResponseDto result = fallbackService.generateFallbackRoadmap(List.of(javaSkill), "advanced");

        // Advanced is missing, so it should roll backwards into 'beginner' natively
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getVideo()).isEqualTo("http://java-beginner");
    }

    @Test
    void generateFallbackRoadmap_MissingLevelWithNoBeginnerGrabsFirstAvailable() {
        Skill pythonSkill = new Skill();
        pythonSkill.setName("Python");
        SkillContent pythonPro = new SkillContent();
        pythonPro.setLevel("expert");
        pythonPro.setUrl("http://python-pro");
        pythonSkill.setContents(List.of(pythonPro));

        FallbackResponseDto result = fallbackService.generateFallbackRoadmap(List.of(pythonSkill), "intermediate");

        // Beginner is missing too! So it triggers the deepest trapdoor: grab the very first available item
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getVideo()).isEqualTo("http://python-pro");
    }

    @Test
    void generateFallbackRoadmap_EmptySkillsList() {
        FallbackResponseDto result = fallbackService.generateFallbackRoadmap(List.of(), "beginner");
        assertThat(result.getData()).isEmpty();
    }
}
