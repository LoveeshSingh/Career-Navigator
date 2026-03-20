package com.skillbridge.careernavigator.controller;

import com.skillbridge.careernavigator.entity.Role;
import com.skillbridge.careernavigator.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RoleController {

    private final RoleRepository roleRepository;

    @GetMapping
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(role -> new RoleResponse(role.getId().toString(), role.getTitle()))
                .collect(Collectors.toList());
    }

    // Simple DTO for Role list
    public record RoleResponse(String id, String title) {}
}
