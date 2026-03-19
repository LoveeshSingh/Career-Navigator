package com.skillbridge.careernavigator.repository;

import com.skillbridge.careernavigator.entity.RoleSkills;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoleSkillsRepository extends JpaRepository<RoleSkills, UUID> {
}
