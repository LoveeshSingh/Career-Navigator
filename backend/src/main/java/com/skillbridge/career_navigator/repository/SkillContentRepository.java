package com.skillbridge.career_navigator.repository;

import com.skillbridge.career_navigator.entity.SkillContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SkillContentRepository extends JpaRepository<SkillContent, UUID> {
    List<SkillContent> findBySkillIdAndLevel(UUID skillId, String level);
}
