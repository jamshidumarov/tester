package com.example.tester.dto.request;

import com.example.tester.entity.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateQuestionRequest(

        @NotBlank(message = "Savol matni bo'sh bo'lmasligi kerak")
        String text,

        @NotNull(message = "Savol turi ko'rsatilishi shart")
        QuestionType type,

        @NotEmpty(message = "Kamida bitta variant kiritilishi shart")
        @Valid
        List<CreateOptionRequest> options
) {}
