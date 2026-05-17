package com.example.tester.dto.response;

import java.util.List;

public record StudyPageResponse(
        List<AdminQuestionDto> content,
        int currentPage,
        int totalPages,
        long totalElements
) {}
