package com.example.tester.controller;

import com.example.tester.dto.response.SessionSummaryDto;
import com.example.tester.dto.response.StudyPageResponse;
import com.example.tester.service.QuestionService;
import com.example.tester.service.TestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "O'rganish", description = "Savollar ro'yxati va o'tgan natijalar")
public class StudyController {

    private final QuestionService questionService;
    private final TestService testService;

    @Operation(summary = "Savollar ro'yxati (o'rganish rejimi)",
               description = "To'g'ri javoblar bilan barcha savollar. Sahifalash va matn bo'yicha qidiruv qo'llab-quvvatlanadi.")
    @GetMapping("/questions")
    public ResponseEntity<StudyPageResponse> getQuestions(
            @Parameter(description = "Sahifa raqami (0 dan boshlanadi)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sahifadagi savollar soni (max 100)")  @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Savol matni bo'yicha qidiruv")        @RequestParam(defaultValue = "") String search) {
        size = Math.min(size, 100);
        return ResponseEntity.ok(questionService.getStudyQuestions(page, size, search));
    }

    @Operation(summary = "O'tgan natijalarni qidirish",
               description = "Ism va familiya bo'yicha topshirilgan test sessiyalarini qidirish.")
    @GetMapping("/sessions/search")
    public ResponseEntity<List<SessionSummaryDto>> searchSessions(
            @Parameter(description = "Ism",      required = true) @RequestParam String firstName,
            @Parameter(description = "Familiya", required = true) @RequestParam String lastName) {
        return ResponseEntity.ok(testService.searchSessions(firstName, lastName));
    }
}
