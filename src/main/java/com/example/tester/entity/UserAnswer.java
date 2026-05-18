package com.example.tester.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_answers",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_answer_session_question",
                columnNames = {"session_id", "question_id"}
        ),
        indexes = {
                // Unique constraint (session_id, question_id) birinchi ustun bo'lgani uchun
                // findBySessionId uchun alohida index shart emas — composite index ishlaydi
                @Index(name = "idx_ua_question_id", columnList = "question_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private TestSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ElementCollection
    @CollectionTable(
            name = "user_answer_options",
            joinColumns = @JoinColumn(name = "user_answer_id"),
            indexes = @Index(name = "idx_uao_user_answer_id", columnList = "user_answer_id")
    )
    @Column(name = "option_id")
    @Builder.Default
    private List<Long> selectedOptionIds = new ArrayList<>();
}
