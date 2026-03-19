package com.skillbridge.career_navigator.bootstrap;

import com.skillbridge.career_navigator.entity.*;
import com.skillbridge.career_navigator.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final SkillRepository skillRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (roleRepository.count() > 0) {
            return; // Data already seeded
        }

        // 1. Create Role
        Role backendRole = Role.builder().title("Backend Developer").build();
        roleRepository.save(backendRole);

        // 2. Define Skills Data
        String[][] skillData = {
                {"java", "100", "core java", "java 17", "java 21"},
                {"spring boot", "95", "spring", "springboot", "spring security"},
                {"postgresql", "90", "postgres", "psql", "postgresql 15"},
                {"docker", "85", "dockerhub", "docker container", "containerization"},
                {"kubernetes", "80", "k8s", "kube", "kubernetes cluster"},
                {"rest api", "90", "restful api", "rest", "rest architecture"},
                {"microservices", "85", "microservice", "distributed systems", "microservices architecture"},
                {"git", "90", "github", "version control", "gitlab"},
                {"redis", "75", "redis cache", "caching", "in-memory database"},
                {"aws", "80", "amazon web services", "aws cloud", "cloud computing"}
        };

        // 3. Insert Skills, Aliases, Contents, and RoleLinks
        int priorityCounter = 1;
        for (String[] data : skillData) {
            String primaryName = data[0];
            int score = Integer.parseInt(data[1]);

            Skill skill = Skill.builder()
                    .name(primaryName)
                    .score(score)
                    .build();

            // Add Aliases
            for (int i = 2; i < data.length; i++) {
                SkillAlias alias = SkillAlias.builder()
                        .aliasName(data[i].trim().toLowerCase())
                        .skill(skill)
                        .build();
                skill.getAliases().add(alias);
            }

            // Add Content (Videos)
            SkillContent beginnerVideo = SkillContent.builder()
                    .skill(skill)
                    .level("BEGINNER")
                    .url("https://youtube.com/results?search_query=" + primaryName.replace(" ", "+") + "+beginner+tutorial")
                    .build();

            SkillContent intermediateVideo = SkillContent.builder()
                    .skill(skill)
                    .level("INTERMEDIATE")
                    .url("https://youtube.com/results?search_query=" + primaryName.replace(" ", "+") + "+advanced+tutorial")
                    .build();

            skill.getContents().add(beginnerVideo);
            skill.getContents().add(intermediateVideo);

            // Save Skill (Cascades to Aliases and Contents)
            skill = skillRepository.save(skill);

            // Link to Role
            RoleSkills roleSkill = RoleSkills.builder()
                    .id(new RoleSkillsId(backendRole.getId(), skill.getId()))
                    .role(backendRole)
                    .skill(skill)
                    .priority(priorityCounter++)
                    .build();

            backendRole.getRoleSkills().add(roleSkill);
        }

        roleRepository.save(backendRole);
    }
}
