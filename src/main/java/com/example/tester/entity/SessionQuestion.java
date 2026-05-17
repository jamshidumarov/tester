package com.example.tester.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "session_questions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private TestSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private int displayOrder;

    /**
     * Comma-separated option IDs representing the shuffled display order, e.g. "3,1,4,2"
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionOrder;
}
