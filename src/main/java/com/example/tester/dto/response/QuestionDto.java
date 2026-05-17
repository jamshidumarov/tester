package com.example.tester.dto.response;

import com.example.tester.entity.QuestionType;

import java.util.List;

public record QuestionDto(
        Long id,
        String text,
        QuestionType type,
        List<OptionDto> options
) {}
