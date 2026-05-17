package com.example.tester.controller;

import com.example.tester.dto.response.SessionSummaryDto;
import com.example.tester.dto.response.StudyPageResponse;
import com.example.tester.service.QuestionService;
import com.example.tester.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StudyController {

    private final QuestionService questionService;
    private final TestService testService;

    /**
     * Barcha savollar (to'g'ri javoblar bilan) — o'rganish rejimi.
     * GET /api/questions?page=0&size=20&search=keyword
     */
    @GetMapping("/questions")
    public ResponseEntity<StudyPageResponse> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String search) {
        size = Math.min(size, 100);
        return ResponseEntity.ok(questionService.getStudyQuestions(page, size, search));
    }

    /**
     * Ism va familiya bo'yicha o'tgan test natijalarini qidirish.
     * GET /api/sessions/search?firstName=Ali&lastName=Valiyev
     */
    @GetMapping("/sessions/search")
    public ResponseEntity<List<SessionSummaryDto>> searchSessions(
            @RequestParam String firstName,
            @RequestParam String lastName) {
        return ResponseEntity.ok(testService.searchSessions(firstName, lastName));
    }
}
