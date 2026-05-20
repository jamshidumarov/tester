package com.example.tester.service;

import com.example.tester.dto.request.CreateQuestionRequest;
import com.example.tester.dto.response.AdminQuestionDto;
import com.example.tester.dto.response.StudyPageResponse;
import com.example.tester.entity.Question;
import com.example.tester.entity.QuestionOption;
import com.example.tester.entity.QuestionType;
import com.example.tester.exception.AppException;
import com.example.tester.repository.QuestionOptionRepository;
import com.example.tester.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;

    @Transactional(readOnly = true)
    public List<AdminQuestionDto> getAllQuestions() {
        List<Question> questions = questionRepository.findAll();
        List<Long> ids = questions.stream().map(Question::getId).toList();
        if (ids.isEmpty()) return List.of();
        List<Question> withOptions = questionRepository.findAllWithOptionsByIds(ids);
        return withOptions.stream().map(this::toAdminDto).toList();
    }

    @Transactional(readOnly = true)
    public long getTotalCount() {
        return questionRepository.count();
    }

    public AdminQuestionDto createQuestion(CreateQuestionRequest request) {
        long correctCount = request.options().stream().filter(o -> o.correct()).count();

        if (correctCount == 0) {
            throw AppException.badRequest("Kamida bitta to'g'ri javob bo'lishi shart");
        }
        if (request.type() == QuestionType.SINGLE_CHOICE && correctCount != 1) {
            throw AppException.badRequest("Bir tanlovli (SINGLE_CHOICE) savolda aynan 1 ta to'g'ri javob bo'lishi kerak");
        }
        if (request.type() == QuestionType.MULTIPLE_CHOICE && correctCount < 2) {
            throw AppException.badRequest("Ko'p tanlovli (MULTIPLE_CHOICE) savolda kamida 2 ta to'g'ri javob bo'lishi kerak");
        }

        Question question = Question.builder()
                .text(request.text())
                .type(request.type())
                .options(new ArrayList<>())
                .build();
        question = questionRepository.save(question);

        for (var opt : request.options()) {
            QuestionOption option = QuestionOption.builder()
                    .question(question)
                    .text(opt.text())
                    .correct(opt.correct())
                    .build();
            question.getOptions().add(questionOptionRepository.save(option));
        }

        return toAdminDto(question);
    }

    @Transactional(readOnly = true)
    public StudyPageResponse getStudyQuestions(int page, int size, String search, Long subjectId) {
        List<Question> filtered;
        if (subjectId != null) {
            filtered = (search == null || search.isBlank())
                    ? questionRepository.findBySubjectId(subjectId)
                    : questionRepository.findBySubjectIdAndTextContaining(subjectId, search.trim());
        } else {
            filtered = (search == null || search.isBlank())
                    ? questionRepository.findAll()
                    : questionRepository.findByTextContaining(search.trim());
        }

        int total = filtered.size();
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);

        List<Long> pageIds = filtered.subList(from, to).stream().map(Question::getId).toList();
        if (pageIds.isEmpty()) {
            return new StudyPageResponse(List.of(), page, totalPages, total);
        }

        Map<Long, Question> withOptions = questionRepository.findAllWithOptionsByIds(pageIds)
                .stream().collect(Collectors.toMap(Question::getId, q -> q));

        List<AdminQuestionDto> content = pageIds.stream()
                .map(qid -> toAdminDto(withOptions.get(qid)))
                .toList();

        return new StudyPageResponse(content, page, totalPages, total);
    }

    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw AppException.notFound("Savol topilmadi: " + id);
        }
        questionRepository.deleteById(id);
    }

    private AdminQuestionDto toAdminDto(Question q) {
        List<AdminQuestionDto.AdminOptionDto> options = q.getOptions().stream()
                .map(o -> new AdminQuestionDto.AdminOptionDto(o.getId(), o.getText(), o.isCorrect()))
                .toList();
        return new AdminQuestionDto(q.getId(), q.getText(), q.getType(), options);
    }
}
