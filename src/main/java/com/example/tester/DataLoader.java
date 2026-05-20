package com.example.tester;

import com.example.tester.entity.Question;
import com.example.tester.entity.QuestionOption;
import com.example.tester.entity.QuestionType;
import com.example.tester.entity.Subject;
import com.example.tester.repository.QuestionOptionRepository;
import com.example.tester.repository.QuestionRepository;
import com.example.tester.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final SubjectRepository subjectRepository;

    private static final PathMatchingResourcePatternResolver RESOLVER =
            new PathMatchingResourcePatternResolver();

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        Resource[] resources = RESOLVER.getResources("classpath*:static/*.doc*");
        Arrays.sort(resources, Comparator.comparing(r -> r.getFilename() != null ? r.getFilename() : ""));

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename == null) continue;
            if (!filename.endsWith(".doc") && !filename.endsWith(".docx")) continue;

            log.info("Fayl qayta ishlanmoqda: {}", filename);
            try {
                List<String> lines;
                try (InputStream is = resource.getInputStream()) {
                    lines = filename.endsWith(".docx")
                            ? extractLinesFromDocx(is)
                            : extractLinesFromDoc(is);
                }
                processLines(lines);
            } catch (Exception e) {
                log.error("'{}' faylni qayta ishlashda xato: {}", filename, e.getMessage(), e);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Per-file processing
    // -----------------------------------------------------------------------

    private void processLines(List<String> lines) {
        String subjectName = extractSubjectName(lines);

        Subject subject = subjectRepository.findByName(subjectName)
                .orElseGet(() -> {
                    log.info("'{}' fani yaratilmoqda...", subjectName);
                    return subjectRepository.save(Subject.builder().name(subjectName).build());
                });

        // Subjectsiz qolgan savollarni ushbu subjectga biriktirish
        List<Question> orphaned = questionRepository.findBySubjectIsNull();
        if (!orphaned.isEmpty()) {
            orphaned.forEach(q -> q.setSubject(subject));
            questionRepository.saveAll(orphaned);
            log.info("{} ta savol '{}' faniga biriktirildi.", orphaned.size(), subjectName);
        }

        long existingCount = questionRepository.countBySubject(subject);
        if (existingCount > 0) {
            log.info("'{}' fanida {} ta savol allaqachon mavjud, o'tkazib yuborildi.",
                    subjectName, existingCount);
            return;
        }

        List<ParsedQuestion> parsed = parseLines(lines);
        saveAll(parsed, subject);
        log.info("'{}' fani uchun {} ta savol yuklandi.", subjectName, parsed.size());
    }

    // -----------------------------------------------------------------------
    // Line extraction
    // -----------------------------------------------------------------------

    private List<String> extractLinesFromDocx(InputStream is) throws Exception {
        List<String> lines = new ArrayList<>();
        try (XWPFDocument doc = new XWPFDocument(is)) {
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                String line = paragraph.getText().trim();
                if (!line.isEmpty()) lines.add(line);
            }
        }
        return lines;
    }

    private List<String> extractLinesFromDoc(InputStream is) throws Exception {
        List<String> lines = new ArrayList<>();
        try (HWPFDocument doc = new HWPFDocument(is)) {
            var range = doc.getRange();
            for (int i = 0; i < range.numParagraphs(); i++) {
                // .doc paragraphs end with \r and may contain null bytes
                String line = sanitize(range.getParagraph(i).text());
                if (!line.isEmpty()) lines.add(line);
            }
        }
        return lines;
    }

    private static String sanitize(String raw) {
        StringBuilder sb = new StringBuilder(raw.length());
        for (char c : raw.toCharArray()) {
            if (c != '\r' && c != '\0') sb.append(c);
        }
        return sb.toString().trim();
    }

    // -----------------------------------------------------------------------
    // Shared parsing
    // -----------------------------------------------------------------------

    private String extractSubjectName(List<String> lines) {
        for (String line : lines) {
            if (!line.startsWith("#") && !line.startsWith("+") && !line.startsWith("-")) {
                return line;
            }
            break;
        }
        return "Noma'lum fan";
    }

    private List<ParsedQuestion> parseLines(List<String> lines) {
        List<ParsedQuestion> result = new ArrayList<>();
        ParsedQuestion current = null;
        int skipped = 0;

        for (String line : lines) {
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

        if (current != null) {
            if (current.isValid()) {
                current.fixCorrectIfMissing();
                result.add(current);
            } else {
                skipped++;
            }
        }

        if (skipped > 0) log.warn("{} ta savol o'tkazib yuborildi.", skipped);
        return result;
    }

    // -----------------------------------------------------------------------
    // Saving
    // -----------------------------------------------------------------------

    private void saveAll(List<ParsedQuestion> parsedList, Subject subject) {
        for (ParsedQuestion pq : parsedList) {
            long correctCount = pq.options.stream().filter(o -> o.correct).count();
            QuestionType type = correctCount > 1
                    ? QuestionType.MULTIPLE_CHOICE
                    : QuestionType.SINGLE_CHOICE;

            Question question = questionRepository.save(Question.builder()
                    .text(pq.text)
                    .type(type)
                    .subject(subject)
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

        ParsedQuestion(String text) { this.text = text; }

        boolean isValid() { return !text.isBlank() && !options.isEmpty(); }

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
