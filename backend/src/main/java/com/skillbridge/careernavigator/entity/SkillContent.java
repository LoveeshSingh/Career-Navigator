package com.skillbridge.careernavigator.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "skill_content", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"skill_id", "level", "url"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "level", length = 20, nullable = false)
    private String level;

    @Column(name = "resource_type", length = 50, nullable = false)
    @Builder.Default
    private String resourceType = "YOUTUBE_VIDEO";

    @Column(name = "url", columnDefinition = "TEXT", nullable = false)
    private String url;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
