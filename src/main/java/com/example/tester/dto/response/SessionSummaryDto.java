package com.example.tester.dto.response;

import java.time.LocalDateTime;

public record SessionSummaryDto(
        Long sessionId,
        String firstName,
        String lastName,
        int totalQuestions,
        Integer correctCount,
        Double scorePercent,
        long timeTakenSeconds,
        LocalDateTime startedAt,
        LocalDateTime submittedAt
) {}
