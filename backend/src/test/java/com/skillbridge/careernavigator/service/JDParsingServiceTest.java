package com.skillbridge.careernavigator.service;

import com.skillbridge.careernavigator.dto.ParsedSkillDto;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.entity.SkillAlias;
import com.skillbridge.careernavigator.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JDParsingServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private JDParsingService jdParsingService;

    private List<Skill> mockDbSkills;

    @BeforeEach
    void setUp() {
        mockDbSkills = new ArrayList<>();

        // Setup Java skill
        Skill javaSkill = Skill.builder()
                .id(UUID.randomUUID())
                .name("java")
                .score(100)
                .aliases(new ArrayList<>())
                .build();
        javaSkill.getAliases().add(SkillAlias.builder().aliasName("core java").skill(javaSkill).build());

        // Setup Spring Boot skill
        Skill springSkill = Skill.builder()
                .id(UUID.randomUUID())
                .name("spring boot")
                .score(90)
                .aliases(new ArrayList<>())
                .build();

        // Setup AWS skill
        Skill awsSkill = Skill.builder()
                .id(UUID.randomUUID())
                .name("aws")
                .score(80)
                .aliases(new ArrayList<>())
                .build();
        
        mockDbSkills.add(javaSkill);
        mockDbSkills.add(springSkill);
        mockDbSkills.add(awsSkill);
    }

    @Test
    void parseJobDescription_EmptyOrNull_ReturnsEmpty() {
        assertTrue(jdParsingService.parseJobDescription("").isEmpty());
        assertTrue(jdParsingService.parseJobDescription("   ").isEmpty());
        assertTrue(jdParsingService.parseJobDescription(null).isEmpty());
    }

    @Test
    void parseJobDescription_RequiredKeyword_AddsBonus() {
        when(skillRepository.findAll()).thenReturn(mockDbSkills);

        // Java is required (occurrence in chunk1 = 1 + required=10 + freq=2 = 12. occurrence in chunk2 = 1 + freq=2 = 2. Total = 14). Score = 14 + 0.1 (tie breaker)
        String jd = "We are hiring. Java is required. We also use Java.";
        
        List<ParsedSkillDto> result = jdParsingService.parseJobDescription(jd);

        assertEquals(1, result.size());
        assertEquals("java", result.get(0).getSkill().getName());
        assertEquals(14.1, result.get(0).getScore(), 0.01);
    }

    @Test
    void parseJobDescription_PreferredKeyword_AddsBonus() {
        when(skillRepository.findAll()).thenReturn(mockDbSkills);

        // Spring Boot is preferred (base occurrence = 2, preferred bonus = 5). Score = 7 + 0.09 = 7.09
        String jd = "Spring Boot is preferred.";
        
        List<ParsedSkillDto> result = jdParsingService.parseJobDescription(jd);

        assertEquals(1, result.size());
        assertEquals("spring boot", result.get(0).getSkill().getName());
        assertEquals(7.09, result.get(0).getScore(), 0.01);
    }

    @Test
    void parseJobDescription_MultipleSkills_CorrectlyScored() {
        when(skillRepository.findAll()).thenReturn(mockDbSkills);

        String jd = "Must have Java and Spring Boot. Nice to have AWS.";
        
        List<ParsedSkillDto> result = jdParsingService.parseJobDescription(jd);

        assertEquals(3, result.size());

        // Verify all 3 were extracted
        assertTrue(result.stream().anyMatch(dto -> dto.getSkill().getName().equals("java")));
        assertTrue(result.stream().anyMatch(dto -> dto.getSkill().getName().equals("spring boot")));
        assertTrue(result.stream().anyMatch(dto -> dto.getSkill().getName().equals("aws")));
    }
}
