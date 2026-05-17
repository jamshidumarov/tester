package com.example.tester.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmitTestRequest(

        @NotEmpty(message = "Javoblar ro'yxati bo'sh bo'lmasligi kerak")
        List<AnswerDto> answers
) {}
