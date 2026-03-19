package com.skillbridge.career_navigator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleSkillsId implements Serializable {

    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "skill_id")
    private UUID skillId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleSkillsId that = (RoleSkillsId) o;
        return Objects.equals(roleId, that.roleId) &&
               Objects.equals(skillId, that.skillId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, skillId);
    }
}
