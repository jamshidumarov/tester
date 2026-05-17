package com.example.tester.dto.request;

import java.util.List;

public record AnswerDto(
        Long questionId,
        List<Long> selectedOptionIds
) {}
