package com.example.tester.dto.response;

import java.util.List;

public record StartTestResponse(
        Long sessionId,
        String firstName,
        String lastName,
        int timeLimitMinutes,
        int totalQuestions,
        List<QuestionDto> questions
) {}
