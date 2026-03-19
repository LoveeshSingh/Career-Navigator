package com.skillbridge.careernavigator.service;

import com.skillbridge.careernavigator.dto.ValidatedSkillDto;
import com.skillbridge.careernavigator.entity.Role;
import com.skillbridge.careernavigator.entity.RoleSkills;
import com.skillbridge.careernavigator.entity.Skill;
import com.skillbridge.careernavigator.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillSelectionServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private SkillSelectionService skillSelectionService;

    private Skill javaSkill, pythonSkill, awsSkill;

    @BeforeEach
    void setUp() {
        javaSkill = new Skill();
        javaSkill.setName("Java");

        pythonSkill = new Skill();
        pythonSkill.setName("Python");

        awsSkill = new Skill();
        awsSkill.setName("AWS");
    }

    @Test
    void selectSkillsForJd_CorrectTopKSortedSelection() {
        ValidatedSkillDto dto1 = new ValidatedSkillDto(javaSkill, 9.8);
        ValidatedSkillDto dto2 = new ValidatedSkillDto(pythonSkill, 9.5);
        ValidatedSkillDto dto3 = new ValidatedSkillDto(awsSkill, 9.9);

        // Act - request top 2, should sort DESC natively
        List<Skill> result = skillSelectionService.selectSkillsForJd(Arrays.asList(dto1, dto2, dto3), 2);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("AWS"); // 9.9
        assertThat(result.get(1).getName()).isEqualTo("Java"); // 9.8
    }

    @Test
    void selectSkillsForJd_KGreaterThanAvailable() {
        ValidatedSkillDto dto1 = new ValidatedSkillDto(javaSkill, 9.8);
        
        List<Skill> result = skillSelectionService.selectSkillsForJd(List.of(dto1), 5); // Request 5, have 1

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Java");
    }

    @Test
    void selectSkillsForRole_OrderingPreservedByPriority() {
        UUID roleId = UUID.randomUUID(); // Simulation wrapper
        Role mockRole = new Role();
        
        RoleSkills rs1 = new RoleSkills(); rs1.setSkill(javaSkill);
        RoleSkills rs2 = new RoleSkills(); rs2.setSkill(pythonSkill);
        RoleSkills rs3 = new RoleSkills(); rs3.setSkill(awsSkill);

        // Simulating physical ordering from Query @OrderBy
        mockRole.setRoleSkills(Arrays.asList(rs1, rs2, rs3)); 
        
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(mockRole));

        // Act - Request top 2
        List<Skill> result = skillSelectionService.selectSkillsForRole(roleId, 2);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Java");
        assertThat(result.get(1).getName()).isEqualTo("Python");
    }
}
