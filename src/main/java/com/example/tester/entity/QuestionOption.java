package com.example.tester.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_options", indexes = {
        // JOIN FETCH q.options va calculateResults uchun asosiy index
        @Index(name = "idx_option_question_id",         columnList = "question_id"),
        // To'g'ri javoblarni saralash uchun qo'shimcha index
        @Index(name = "idx_option_question_id_correct", columnList = "question_id, correct")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false)
    private boolean correct;
}
