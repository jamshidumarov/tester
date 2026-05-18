package com.example.tester.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_sessions", indexes = {
        // findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndStatus uchun
        @Index(name = "idx_session_name_status", columnList = "first_name, last_name, status"),
        // Tarixni sana bo'yicha saralash uchun
        @Index(name = "idx_session_started_at", columnList = "started_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private int questionCount;

    @Column(nullable = false)
    private int timeLimitMinutes;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    private Integer correctCount;
}
