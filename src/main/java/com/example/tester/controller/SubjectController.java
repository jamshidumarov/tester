package com.example.tester.controller;

import com.example.tester.dto.response.SubjectDto;
import com.example.tester.repository.QuestionRepository;
import com.example.tester.repository.SubjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Tag(name = "Fanlar", description = "Mavjud fanlar ro'yxati")
public class SubjectController {

    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;

    @Operation(summary = "Barcha fanlarni olish")
    @GetMapping
    public ResponseEntity<List<SubjectDto>> getSubjects() {
        List<SubjectDto> subjects = subjectRepository.findAll().stream()
                .map(s -> new SubjectDto(s.getId(), s.getName(),
                        questionRepository.countBySubjectId(s.getId())))
                .toList();
        return ResponseEntity.ok(subjects);
    }
}
