package com.example.tester;

import com.example.tester.entity.Question;
import com.example.tester.entity.QuestionOption;
import com.example.tester.entity.QuestionType;
import com.example.tester.repository.QuestionOptionRepository;
import com.example.tester.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (questionRepository.count() > 0) {
            log.info("Bazada savollar allaqachon mavjud, yuklash o'tkazib yuborildi.");
            return;
        }

        log.info("tests.docx dan savollar yuklanmoqda...");

        ClassPathResource resource = new ClassPathResource("static/tests.docx");
        try (InputStream is = resource.getInputStream();
             XWPFDocument doc = new XWPFDocument(is)) {

            List<ParsedQuestion> parsed = parseDocx(doc);
            saveAll(parsed);
            log.info("{} ta savol muvaffaqiyatli yuklandi.", parsed.size());
        }
    }

    // -----------------------------------------------------------------------
    // Parsing
    // -----------------------------------------------------------------------

    private List<ParsedQuestion> parseDocx(XWPFDocument doc) {
        List<ParsedQuestion> result = new ArrayList<>();
        ParsedQuestion current = null;
        int skipped = 0;

        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            String line = paragraph.getText().trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("#")) {
                if (current != null) {
                    if (current.isValid()) {
                        current.fixCorrectIfMissing();
                        result.add(current);
                    } else {
                        skipped++;
                        log.warn("Varianti yo'q savol o'tkazib yuborildi: \"{}\"",
                                current.text.length() > 60 ? current.text.substring(0, 60) + "..." : current.text);
                    }
                }
                current = new ParsedQuestion(line.substring(1).trim());

            } else if (line.startsWith("+") && current != null) {
                current.options.add(new ParsedOption(line.substring(1).trim(), true));

            } else if (line.startsWith("-") && current != null) {
                current.options.add(new ParsedOption(line.substring(1).trim(), false));
            }
        }

        // Oxirgi savolni qo'shish
        if (current != null) {
            if (current.isValid()) {
                current.fixCorrectIfMissing();
                result.add(current);
            } else {
                skipped++;
            }
        }

        if (skipped > 0) {
            log.warn("{} ta savol o'tkazib yuborildi (to'g'ri javobsiz).", skipped);
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // Saving
    // -----------------------------------------------------------------------

    private void saveAll(List<ParsedQuestion> parsedList) {
        for (ParsedQuestion pq : parsedList) {
            long correctCount = pq.options.stream().filter(o -> o.correct).count();
            QuestionType type = correctCount > 1
                    ? QuestionType.MULTIPLE_CHOICE
                    : QuestionType.SINGLE_CHOICE;

            Question question = questionRepository.save(Question.builder()
                    .text(pq.text)
                    .type(type)
                    .options(new ArrayList<>())
                    .build());

            for (ParsedOption opt : pq.options) {
                questionOptionRepository.save(QuestionOption.builder()
                        .question(question)
                        .text(opt.text)
                        .correct(opt.correct)
                        .build());
            }
        }
    }

    // -----------------------------------------------------------------------
    // Inner helper classes
    // -----------------------------------------------------------------------

    private static class ParsedQuestion {
        final String text;
        final List<ParsedOption> options = new ArrayList<>();

        ParsedQuestion(String text) {
            this.text = text;
        }

        boolean isValid() {
            return !text.isBlank() && !options.isEmpty();
        }

        void fixCorrectIfMissing() {
            boolean hasCorrect = options.stream().anyMatch(o -> o.correct);
            if (!hasCorrect && !options.isEmpty()) {
                ParsedOption first = options.get(0);
                options.set(0, new ParsedOption(first.text(), true));
            }
        }
    }

    private record ParsedOption(String text, boolean correct) {}
}
