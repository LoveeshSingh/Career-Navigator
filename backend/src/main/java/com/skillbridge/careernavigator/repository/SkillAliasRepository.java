package com.skillbridge.careernavigator.repository;

import com.skillbridge.careernavigator.entity.SkillAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillAliasRepository extends JpaRepository<SkillAlias, UUID> {
    Optional<SkillAlias> findByAliasName(String aliasName);
    Optional<SkillAlias> findByAliasNameIgnoreCase(String aliasName);
    List<SkillAlias> findBySkillIdIn(List<UUID> skillIds);
}
