package com.example.tester.service;

import com.example.tester.dto.request.AnswerDto;
import com.example.tester.dto.request.StartTestRequest;
import com.example.tester.dto.request.SubmitTestRequest;
import com.example.tester.dto.response.OptionDto;
import com.example.tester.dto.response.QuestionDto;
import com.example.tester.dto.response.SessionSummaryDto;
import com.example.tester.dto.response.StartTestResponse;
import com.example.tester.dto.response.TestResultResponse;
import com.example.tester.dto.response.WrongAnswerDto;
import com.example.tester.entity.*;
import com.example.tester.exception.AppException;
import com.example.tester.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TestService {

    private final TestSessionRepository sessionRepository;
    private final SessionQuestionRepository sessionQuestionRepository;
    private final QuestionRepository questionRepository;
    private final UserAnswerRepository userAnswerRepository;

    public StartTestResponse startTest(StartTestRequest request) {
        long totalAvailable = questionRepository.count();
        if (request.questionCount() > totalAvailable) {
            throw AppException.badRequest(
                    "Bazada faqat " + totalAvailable + " ta savol mavjud, " +
                    request.questionCount() + " ta so'raldingiz.");
        }

        // Random savollarni olish (faqat ID lar)
        List<Question> randomQuestions = questionRepository.findRandomQuestions(request.questionCount());
        List<Long> ids = randomQuestions.stream().map(Question::getId).toList();

        // Savol variantlarini (options) ham bir queryda yuklash — N+1 muammosidan saqlanish
        Map<Long, Question> questionMap = questionRepository.findAllWithOptionsByIds(ids)
                .stream().collect(Collectors.toMap(Question::getId, q -> q));

        // Sessiya yaratish
        TestSession session = sessionRepository.save(TestSession.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .questionCount(request.questionCount())
                .timeLimitMinutes(request.timeLimitMinutes())
                .startedAt(LocalDateTime.now())
                .status(SessionStatus.STARTED)
                .build());

        // Har bir savol uchun variantlarni aralashtirish va sessiyaga saqlash
        List<QuestionDto> questionDtos = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Question q = questionMap.get(ids.get(i));

            List<QuestionOption> shuffledOptions = new ArrayList<>(q.getOptions());
            Collections.shuffle(shuffledOptions);

            String optionOrder = shuffledOptions.stream()
                    .map(o -> o.getId().toString())
                    .collect(Collectors.joining(","));

            sessionQuestionRepository.save(SessionQuestion.builder()
                    .session(session)
                    .question(q)
                    .displayOrder(i + 1)
                    .optionOrder(optionOrder)
                    .build());

            List<OptionDto> optionDtos = shuffledOptions.stream()
                    .map(o -> new OptionDto(o.getId(), o.getText()))
                    .toList();

            questionDtos.add(new QuestionDto(q.getId(), q.getText(), q.getType(), optionDtos));
        }

        return new StartTestResponse(
                session.getId(),
                session.getFirstName(),
                session.getLastName(),
                session.getTimeLimitMinutes(),
                questionDtos.size(),
                questionDtos);
    }

    @Transactional(readOnly = true)
    public StartTestResponse getSessionQuestions(Long sessionId) {
        TestSession session = findSessionOrThrow(sessionId);

        List<SessionQuestion> sessionQuestions =
                sessionQuestionRepository.findBySessionIdWithQuestionsAndOptions(sessionId);

        List<QuestionDto> questionDtos = sessionQuestions.stream()
                .map(this::buildQuestionDto)
                .toList();

        return new StartTestResponse(
                session.getId(),
                session.getFirstName(),
                session.getLastName(),
                session.getTimeLimitMinutes(),
                questionDtos.size(),
                questionDtos);
    }

    public TestResultResponse submitTest(Long sessionId, SubmitTestRequest request) {
        TestSession session = findSessionOrThrow(sessionId);

        if (session.getStatus() == SessionStatus.SUBMITTED) {
            throw AppException.conflict("Bu test allaqachon topshirilgan");
        }

        // Foydalanuvchi javoblarini saqlash
        for (AnswerDto answer : request.answers()) {
            List<Long> selectedIds = answer.selectedOptionIds() != null
                    ? answer.selectedOptionIds()
                    : Collections.emptyList();

            UserAnswer ua = UserAnswer.builder()
                    .session(session)
                    .question(questionRepository.getReferenceById(answer.questionId()))
                    .selectedOptionIds(new ArrayList<>(selectedIds))
                    .build();
            userAnswerRepository.save(ua);
        }

        // Sessiyani yopish (natija hisoblashdan OLDIN — timeTakenSeconds to'g'ri chiqishi uchun)
        session.setStatus(SessionStatus.SUBMITTED);
        session.setSubmittedAt(LocalDateTime.now());
        sessionRepository.save(session);

        TestResultResponse result = calculateResults(session);
        session.setCorrectCount(result.correctCount());
        return result;
    }

    @Transactional(readOnly = true)
    public TestResultResponse getResults(Long sessionId) {
        TestSession session = findSessionOrThrow(sessionId);
        if (session.getStatus() != SessionStatus.SUBMITTED) {
            throw AppException.badRequest("Test hali topshirilmagan");
        }
        return calculateResults(session);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private TestResultResponse calculateResults(TestSession session) {
        List<SessionQuestion> sessionQuestions =
                sessionQuestionRepository.findBySessionIdWithQuestionsAndOptions(session.getId());

        Map<Long, UserAnswer> answerMap = userAnswerRepository.findBySessionId(session.getId())
                .stream().collect(Collectors.toMap(ua -> ua.getQuestion().getId(), ua -> ua));

        int correct = 0;
        List<WrongAnswerDto> wrongAnswers = new ArrayList<>();

        for (int i = 0; i < sessionQuestions.size(); i++) {
            SessionQuestion sq = sessionQuestions.get(i);
            Question q = sq.getQuestion();

            Set<Long> correctIds = q.getOptions().stream()
                    .filter(QuestionOption::isCorrect)
                    .map(QuestionOption::getId)
                    .collect(Collectors.toSet());

            UserAnswer ua = answerMap.get(q.getId());
            Set<Long> selectedIds = (ua != null && ua.getSelectedOptionIds() != null)
                    ? new HashSet<>(ua.getSelectedOptionIds())
                    : Collections.emptySet();

            if (selectedIds.equals(correctIds)) {
                correct++;
            } else {
                Map<Long, String> optionTexts = q.getOptions().stream()
                        .collect(Collectors.toMap(QuestionOption::getId, QuestionOption::getText));

                List<String> yourAnswerTexts = selectedIds.isEmpty()
                        ? List.of("Javob berilmagan")
                        : selectedIds.stream().map(id -> optionTexts.getOrDefault(id, "Noma'lum")).toList();

                List<String> correctAnswerTexts = correctIds.stream()
                        .map(id -> optionTexts.getOrDefault(id, "Noma'lum"))
                        .toList();

                wrongAnswers.add(new WrongAnswerDto(
                        sq.getDisplayOrder(),
                        q.getId(),
                        q.getText(),
                        q.getType(),
                        yourAnswerTexts,
                        correctAnswerTexts));
            }
        }

        int total = sessionQuestions.size();
        double score = total > 0 ? Math.round((double) correct / total * 1000.0) / 10.0 : 0.0;

        long timeTaken = 0;
        if (session.getSubmittedAt() != null) {
            timeTaken = Duration.between(session.getStartedAt(), session.getSubmittedAt()).getSeconds();
        }

        return new TestResultResponse(
                session.getFirstName(),
                session.getLastName(),
                total,
                correct,
                total - correct,
                score,
                timeTaken,
                wrongAnswers);
    }

    private QuestionDto buildQuestionDto(SessionQuestion sq) {
        Question q = sq.getQuestion();
        Map<Long, QuestionOption> optionMap = q.getOptions().stream()
                .collect(Collectors.toMap(QuestionOption::getId, o -> o));

        List<OptionDto> options = Arrays.stream(sq.getOptionOrder().split(","))
                .map(idStr -> {
                    QuestionOption opt = optionMap.get(Long.parseLong(idStr));
                    return new OptionDto(opt.getId(), opt.getText());
                })
                .toList();

        return new QuestionDto(q.getId(), q.getText(), q.getType(), options);
    }

    @Transactional(readOnly = true)
    public List<SessionSummaryDto> searchSessions(String firstName, String lastName) {
        return sessionRepository
                .findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndStatusOrderByStartedAtDesc(
                        firstName, lastName, SessionStatus.SUBMITTED)
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    private SessionSummaryDto toSummaryDto(TestSession s) {
        Double scorePercent = null;
        if (s.getCorrectCount() != null) {
            scorePercent = Math.round((double) s.getCorrectCount() / s.getQuestionCount() * 1000.0) / 10.0;
        }
        long timeTaken = s.getSubmittedAt() != null
                ? Duration.between(s.getStartedAt(), s.getSubmittedAt()).getSeconds()
                : 0;

        return new SessionSummaryDto(
                s.getId(), s.getFirstName(), s.getLastName(),
                s.getQuestionCount(), s.getCorrectCount(), scorePercent,
                timeTaken, s.getStartedAt(), s.getSubmittedAt());
    }

    private TestSession findSessionOrThrow(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> AppException.notFound("Sessiya topilmadi: " + sessionId));
    }
}
