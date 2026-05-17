package com.example.tester.dto.response;

import com.example.tester.entity.QuestionType;

import java.util.List;

public record WrongAnswerDto(
        int questionNumber,
        Long questionId,
        String questionText,
        QuestionType questionType,
        List<String> yourAnswers,
        List<String> correctAnswers
) {}
