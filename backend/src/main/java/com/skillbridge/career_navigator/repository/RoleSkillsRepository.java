package com.skillbridge.career_navigator.repository;

import com.skillbridge.career_navigator.entity.RoleSkills;
import com.skillbridge.career_navigator.entity.RoleSkillsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoleSkillsRepository extends JpaRepository<RoleSkills, UUID> {
}
