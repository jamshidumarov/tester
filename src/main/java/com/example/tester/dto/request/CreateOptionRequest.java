package com.example.tester.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateOptionRequest(

        @NotBlank(message = "Variant matni bo'sh bo'lmasligi kerak")
        String text,

        boolean correct
) {}
