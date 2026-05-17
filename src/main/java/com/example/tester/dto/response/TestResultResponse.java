package com.example.tester.dto.response;

import java.util.List;

public record TestResultResponse(
        String firstName,
        String lastName,
        int totalQuestions,
        int correctCount,
        int wrongCount,
        double scorePercent,
        long timeTakenSeconds,
        List<WrongAnswerDto> wrongAnswers
) {}
