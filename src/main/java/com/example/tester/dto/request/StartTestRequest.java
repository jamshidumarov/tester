package com.example.tester.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StartTestRequest(

        @NotBlank(message = "Ism kiritilishi shart")
        String firstName,

        @NotBlank(message = "Familiya kiritilishi shart")
        String lastName,

        @Min(value = 1, message = "Savollar soni kamida 1 bo'lishi kerak")
        int questionCount,

        @Min(value = 1, message = "Vaqt limiti kamida 1 daqiqa bo'lishi kerak")
        int timeLimitMinutes,

        Long subjectId
) {}
