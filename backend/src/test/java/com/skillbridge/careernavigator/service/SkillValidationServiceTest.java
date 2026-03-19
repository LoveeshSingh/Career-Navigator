package com.skillbridge.careernavigator.service;

import com.skillbridge.careernavigator.dto.ExtractedSkillDto;
import com.skillbridge.careernavigator.dto.ValidatedSkillDto;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.entity.SkillAlias;
import com.skillbridge.careernavigator.repository.SkillAliasRepository;
import com.skillbridge.careernavigator.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillValidationServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private SkillAliasRepository skillAliasRepository;

    @InjectMocks
    private SkillValidationService skillValidationService;

    private Skill javaSkill;
    private Skill pythonSkill;

    @BeforeEach
    void setUp() {
        javaSkill = new Skill();
        javaSkill.setName("Java");

        pythonSkill = new Skill();
        pythonSkill.setName("Python");
    }

    @Test
    void validateSkills_ExactMatch() {
        // Arrange
        ExtractedSkillDto inputSkill = new ExtractedSkillDto("Java", 9.5);
        when(skillRepository.findByNameIgnoreCase("java")).thenReturn(Optional.of(javaSkill));

        // Act
        List<ValidatedSkillDto> result = skillValidationService.validateSkills(List.of(inputSkill));

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSkill().getName()).isEqualTo("Java");
        assertThat(result.get(0).getImportanceScore()).isEqualTo(9.5);
        verify(skillRepository, times(1)).findByNameIgnoreCase("java");
        verifyNoInteractions(skillAliasRepository);
    }

    @Test
    void validateSkills_AliasMatch() {
        // Arrange
        ExtractedSkillDto inputSkill = new ExtractedSkillDto("java script", 8.0);
        Skill jsSkill = new Skill();
        jsSkill.setName("JavaScript");
        
        SkillAlias jsAlias = new SkillAlias();
        jsAlias.setSkill(jsSkill);
        jsAlias.setAliasName("java script");

        when(skillRepository.findByNameIgnoreCase("java script")).thenReturn(Optional.empty());
        when(skillAliasRepository.findByAliasNameIgnoreCase("java script")).thenReturn(Optional.of(jsAlias));

        // Act
        List<ValidatedSkillDto> result = skillValidationService.validateSkills(List.of(inputSkill));

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSkill().getName()).isEqualTo("JavaScript");
        assertThat(result.get(0).getImportanceScore()).isEqualTo(8.0);
    }

    @Test
    void validateSkills_UnknownSkillIgnored() {
        // Arrange
        ExtractedSkillDto inputSkill = new ExtractedSkillDto("Unknown Framework", 5.0);
        when(skillRepository.findByNameIgnoreCase("unknown framework")).thenReturn(Optional.empty());
        when(skillAliasRepository.findByAliasNameIgnoreCase("unknown framework")).thenReturn(Optional.empty());

        // Act
        List<ValidatedSkillDto> result = skillValidationService.validateSkills(List.of(inputSkill));

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void validateSkills_DuplicatesRemoved() {
        // Arrange
        ExtractedSkillDto input1 = new ExtractedSkillDto("Java", 9.5);
        ExtractedSkillDto input2 = new ExtractedSkillDto("java", 8.0); // Duplicate lowercased
        
        when(skillRepository.findByNameIgnoreCase("java")).thenReturn(Optional.of(javaSkill));

        // Act
        List<ValidatedSkillDto> result = skillValidationService.validateSkills(Arrays.asList(input1, input2));

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getImportanceScore()).isEqualTo(9.5); // Should keep the first/highest importance due to set behavior
    }
}
