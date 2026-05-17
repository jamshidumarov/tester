package com.example.tester.controller;

import com.example.tester.dto.request.StartTestRequest;
import com.example.tester.dto.request.SubmitTestRequest;
import com.example.tester.dto.response.StartTestResponse;
import com.example.tester.dto.response.TestResultResponse;
import com.example.tester.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    /**
     * Test sessiyasini boshlash.
     * Foydalanuvchi ism/familiya, savollar soni va vaqt limitini yuboradi.
     * Javobda: sessionId + aralashtrilgan savollar (to'g'ri javobsiz)
     */
    @PostMapping("/start")
    public ResponseEntity<StartTestResponse> startTest(@Valid @RequestBody StartTestRequest request) {
        return ResponseEntity.ok(testService.startTest(request));
    }

    /**
     * Sessiya savollarini qayta olish (sahifani yangilaganda).
     * Variantlar avvalgi aralash tartibda keladi.
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<StartTestResponse> getSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(testService.getSessionQuestions(sessionId));
    }

    /**
     * Testni topshirish.
     * Javobda: to'g'ri/noto'g'ri sonlar, ball va xato savolllar tafsiloti.
     */
    @PostMapping("/{sessionId}/submit")
    public ResponseEntity<TestResultResponse> submitTest(
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitTestRequest request) {
        return ResponseEntity.ok(testService.submitTest(sessionId, request));
    }

    /**
     * Topshirilgan test natijasini qayta ko'rish.
     */
    @GetMapping("/{sessionId}/result")
    public ResponseEntity<TestResultResponse> getResult(@PathVariable Long sessionId) {
        return ResponseEntity.ok(testService.getResults(sessionId));
    }
}
