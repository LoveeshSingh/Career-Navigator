package com.skillbridge.career_navigator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FallbackResponseDto {
    @Builder.Default
    private String mode = "fallback";
    
    @Builder.Default
    private List<FallbackDataDto> data = new ArrayList<>();
}
