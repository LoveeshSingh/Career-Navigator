package com.skillbridge.career_navigator.repository;

import com.skillbridge.career_navigator.entity.SkillAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillAliasRepository extends JpaRepository<SkillAlias, UUID> {
    Optional<SkillAlias> findByAliasName(String aliasName);
    List<SkillAlias> findBySkillIdIn(List<UUID> skillIds);
}
