package com.example.tester.controller;

import com.example.tester.dto.request.StartTestRequest;
import com.example.tester.dto.request.SubmitTestRequest;
import com.example.tester.dto.response.StartTestResponse;
import com.example.tester.dto.response.TestResultResponse;
import com.example.tester.service.TestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@Tag(name = "Test", description = "Test sessiyasini boshqarish")
public class TestController {

    private final TestService testService;

    @Operation(summary = "Testni boshlash",
               description = "Ism/familiya, savollar soni va vaqt limiti kiritiladi. " +
                             "Javobda sessionId va aralashtrilgan savollar (to'g'ri javobsiz) qaytariladi.")
    @PostMapping("/start")
    public ResponseEntity<StartTestResponse> startTest(@Valid @RequestBody StartTestRequest request) {
        return ResponseEntity.ok(testService.startTest(request));
    }

    @Operation(summary = "Sessiya savollarini olish",
               description = "Sahifa yangilanganda savollarni qayta yuklash uchun. " +
                             "Variantlar avvalgi aralash tartibda keladi.")
    @GetMapping("/{sessionId}")
    public ResponseEntity<StartTestResponse> getSession(
            @Parameter(description = "Sessiya ID") @PathVariable Long sessionId) {
        return ResponseEntity.ok(testService.getSessionQuestions(sessionId));
    }

    @Operation(summary = "Testni topshirish",
               description = "Foydalanuvchi javoblarini yuboradi. " +
                             "Javobda ball, to'g'ri/noto'g'ri sonlar va xato savollar tafsiloti keladi.")
    @PostMapping("/{sessionId}/submit")
    public ResponseEntity<TestResultResponse> submitTest(
            @Parameter(description = "Sessiya ID") @PathVariable Long sessionId,
            @Valid @RequestBody SubmitTestRequest request) {
        return ResponseEntity.ok(testService.submitTest(sessionId, request));
    }

    @Operation(summary = "Test natijasini ko'rish",
               description = "Topshirilgan test natijasini qayta ko'rish.")
    @GetMapping("/{sessionId}/result")
    public ResponseEntity<TestResultResponse> getResult(
            @Parameter(description = "Sessiya ID") @PathVariable Long sessionId) {
        return ResponseEntity.ok(testService.getResults(sessionId));
    }
}
