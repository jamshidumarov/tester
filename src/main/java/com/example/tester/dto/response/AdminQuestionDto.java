package com.example.tester.dto.response;

import com.example.tester.entity.QuestionType;

import java.util.List;

public record AdminQuestionDto(
        Long id,
        String text,
        QuestionType type,
        List<AdminOptionDto> options
) {
    public record AdminOptionDto(Long id, String text, boolean correct) {}
}
