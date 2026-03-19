package com.skillbridge.careernavigator.service;

import com.skillbridge.careernavigator.dto.ResumeMatchResultDto;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.entity.SkillAlias;
import com.skillbridge.careernavigator.repository.SkillAliasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeMatchingServiceTest {

    @Mock
    private SkillAliasRepository skillAliasRepository;

    @InjectMocks
    private ResumeMatchingService resumeMatchingService;

    private Skill javaSkill, pythonSkill, cSharpSkill;

    @BeforeEach
    void setUp() {
        javaSkill = new Skill(); javaSkill.setId(UUID.randomUUID()); javaSkill.setName("Java");
        pythonSkill = new Skill(); pythonSkill.setId(UUID.randomUUID()); pythonSkill.setName("Python");
        cSharpSkill = new Skill(); cSharpSkill.setId(UUID.randomUUID()); cSharpSkill.setName("C#");
    }

    @Test
    void matchSkills_ExactMatchWordBoundariesOnly() {
        String resume = "I have developed many applications using Java and C# recently.";
        
        when(skillAliasRepository.findBySkillIdIn(anyList())).thenReturn(List.of());

        ResumeMatchResultDto result = resumeMatchingService.matchSkills(resume, Arrays.asList(javaSkill, pythonSkill, cSharpSkill));

        assertThat(result.getPresentSkills()).containsExactlyInAnyOrder(javaSkill, cSharpSkill);
        assertThat(result.getMissingSkills()).containsExactly(pythonSkill);
    }

    @Test
    void matchSkills_AliasMatch() {
        String resume = "Proficient in object-oriented python and js programming.";
        Skill jsSkill = new Skill(); jsSkill.setId(UUID.randomUUID()); jsSkill.setName("JavaScript");
        
        SkillAlias jsAlias = new SkillAlias(); jsAlias.setSkill(jsSkill); jsAlias.setAliasName("js");
        
        when(skillAliasRepository.findBySkillIdIn(anyList())).thenReturn(List.of(jsAlias));

        ResumeMatchResultDto result = resumeMatchingService.matchSkills(resume, Arrays.asList(pythonSkill, jsSkill));

        assertThat(result.getPresentSkills()).containsExactlyInAnyOrder(pythonSkill, jsSkill);
        assertThat(result.getMissingSkills()).isEmpty();
    }

    @Test
    void matchSkills_NoMatchBoundaryRules() {
        // "JavaScript" contains "Java", but Regex \b should prevent false positives
        String resume = "I build frontends in JavaScript.";
        
        when(skillAliasRepository.findBySkillIdIn(anyList())).thenReturn(List.of());

        ResumeMatchResultDto result = resumeMatchingService.matchSkills(resume, List.of(javaSkill));

        assertThat(result.getPresentSkills()).isEmpty();
        assertThat(result.getMissingSkills()).containsExactly(javaSkill);
    }

    @Test
    void matchSkills_EmptyResume() {
        ResumeMatchResultDto result = resumeMatchingService.matchSkills("", Arrays.asList(javaSkill, pythonSkill));

        assertThat(result.getPresentSkills()).isEmpty();
        assertThat(result.getMissingSkills()).containsExactlyInAnyOrder(javaSkill, pythonSkill);
        verifyNoInteractions(skillAliasRepository);
    }
}
